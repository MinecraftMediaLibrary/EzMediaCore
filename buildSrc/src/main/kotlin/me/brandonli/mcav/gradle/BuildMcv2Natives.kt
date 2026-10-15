/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.brandonli.mcav.gradle

import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.LocalState
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor

@CacheableTask
abstract class BuildMcv2Natives : DefaultTask() {

    private companion object {

        const val ZIG_TOOL: String = "zig"
        const val ZIG_VERSION_ARGUMENT: String = "version"

        const val NATIVES_PATH: String = "mcav/mcv2/natives"
        const val CHECKSUMS_MANIFEST: String = "SHA256SUMS"
        const val SOURCES_MANIFEST: String = "SOURCES"
        const val CLANG_FORMAT_FILE: String = ".clang-format"

        const val LEVEL_PREFIX: String = "level_"
        const val SOURCE_SUFFIX: String = ".cpp"
        const val OBJECT_SUFFIX: String = ".o"
        const val IMPORT_LIBRARY_EXTENSION: String = "lib"

        const val LINUX: String = "linux"
        const val WINDOWS: String = "windows"
        const val MACOS: String = "macos"

        const val X86_64: String = "x86_64"
        const val AARCH64: String = "aarch64"

        const val LINUX_TARGET_SUFFIX: String = "-linux-gnu.2.28"
        const val WINDOWS_TARGET_SUFFIX: String = "-windows-gnu"
        const val MACOS_TARGET_SUFFIX: String = "-macos.11.0"

        const val LINUX_LIBRARY: String = "libmcv2kernels.so"
        const val WINDOWS_LIBRARY: String = "mcv2kernels.dll"
        const val MACOS_LIBRARY: String = "libmcv2kernels.dylib"

        const val TARGET_OPTION: String = "-target"
        const val OUTPUT_OPTION: String = "-o"

        const val SOURCE_DATE_VARIABLE: String = "SOURCE_DATE_EPOCH"
        const val REPRODUCIBLE_SOURCE_DATE: String = "0"
        const val ZIG_CACHE_VARIABLE: String = "ZIG_GLOBAL_CACHE_DIR"

        const val MANIFEST_SEPARATOR: String = "  "
        const val LINE_SEPARATOR: String = "\n"

        val SVE_VECTOR_BITS: List<Int> = listOf(256, 512)
    }

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourcesDirectory: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val zigExecutable: RegularFileProperty

    @get:Input
    abstract val zigVersion: Property<String>

    @get:Input
    abstract val toolchainChecksum: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:LocalState
    abstract val cacheDirectory: DirectoryProperty

    @get:Internal
    abstract val processCount: Property<Int>

    @get:Inject
    protected abstract val processes: ExecOperations

    @get:Inject
    protected abstract val workers: WorkerExecutor

    @get:Input
    val compilerFlags: List<String> = listOf(
        "-std=c++17", "-O2", "-fPIC", "-fno-exceptions", "-fno-rtti", "-fvisibility=hidden",
        "-ffp-contract=off", "-fwrapv", "-fno-strict-aliasing", "-Wall", "-Wextra", "-Werror"
    )

    @TaskAction
    fun build(): Unit {
        val zig: File = this.zigExecutable.get().asFile
        ToolVersions.verify(this.processes, zig, ZIG_TOOL, this.zigVersion.get(), prefix = "", argument = ZIG_VERSION_ARGUMENT)
        val task: BuildMcv2Natives = this
        this.workers.noIsolation().submit(Work::class.java) {
            this.zigExecutable.set(task.zigExecutable)
            this.sourcesDirectory.set(task.sourcesDirectory)
            this.outputDirectory.set(task.outputDirectory)
            this.objectsDirectory.set(task.temporaryDir)
            this.cacheDirectory.set(task.cacheDirectory)
            this.compilerFlags.set(task.compilerFlags)
            this.processCount.set(task.processCount)
        }
    }

    abstract class Work : WorkAction<Work.Parameters> {

        interface Parameters : WorkParameters {
            val zigExecutable: RegularFileProperty
            val sourcesDirectory: DirectoryProperty
            val outputDirectory: DirectoryProperty
            val objectsDirectory: DirectoryProperty
            val cacheDirectory: DirectoryProperty
            val compilerFlags: ListProperty<String>
            val processCount: Property<Int>
        }

        @get:Inject
        protected abstract val processes: ExecOperations

        override fun execute(): Unit {
            val output: File = this.parameters.outputDirectory.get().asFile
            output.deleteRecursively()
            val natives: File = output.resolve(NATIVES_PATH)
            val objectsDirectory: File = this.parameters.objectsDirectory.get().asFile
            val compilerFlags: List<String> = this.parameters.compilerFlags.get()
            val libraries: MutableList<File> = mutableListOf<File>()
            val compiles: MutableList<() -> Unit> = mutableListOf<() -> Unit>()
            val links: MutableList<() -> Unit> = mutableListOf<() -> Unit>()
            val boundary: List<String> = listOf("-mbranches-within-32B-boundaries")
            val avx512: List<String> = listOf("-mavx512f", "-mavx512dq", "-mavx512bw", "-mavx512vl",
                "-mavx512vbmi", "-mavx512vbmi2", "-mavx512vnni", "-mavx512bitalg")
            for (system in listOf(LINUX, WINDOWS, MACOS)) {
                for (architecture in listOf(X86_64, AARCH64)) {
                    val platform: String = "$system-$architecture"
                    val target: String = architecture + when (system) {
                        LINUX -> LINUX_TARGET_SUFFIX
                        WINDOWS -> WINDOWS_TARGET_SUFFIX
                        else -> MACOS_TARGET_SUFFIX
                    }
                    val units: LinkedHashMap<String, List<String>> = linkedMapOf("scalar" to emptyList<String>())
                    if (architecture == X86_64) {
                        units["sse2"] = emptyList()
                        units["sse41"] = listOf("-msse4.1")
                        units["avx2"] = listOf("-mavx2")
                        if (system != MACOS) {
                            units["avx512"] = avx512
                            units.replaceAll { _, flags -> flags + boundary }
                        }
                    } else {
                        units["neon"] = emptyList()
                        if (system == LINUX) {
                            for (width in SVE_VECTOR_BITS) {
                                units["sve$width"] = listOf("-mcpu=baseline+sve", "-msve-vector-bits=$width")
                            }
                        }
                    }
                    val objects: List<String> = units.map { (level, flags) ->
                        val objectFile: File = objectsDirectory.resolve(platform).resolve("$LEVEL_PREFIX$level$OBJECT_SUFFIX")
                        objectFile.parentFile.mkdirs()
                        compiles += {
                            this.runZig(listOf("c++", TARGET_OPTION, target, "-mcpu=baseline") + compilerFlags + flags +
                                listOf("-c", "$LEVEL_PREFIX$level$SOURCE_SUFFIX", OUTPUT_OPTION, objectFile.absolutePath))
                        }
                        objectFile.absolutePath
                    }
                    val name: String = when (system) {
                        WINDOWS -> WINDOWS_LIBRARY
                        MACOS -> MACOS_LIBRARY
                        else -> LINUX_LIBRARY
                    }
                    val library: File = natives.resolve(platform).resolve(name)
                    library.parentFile.mkdirs()
                    val linkFlags: List<String> = when (system) {
                        LINUX -> listOf("-Wl,-z,noexecstack")
                        MACOS -> listOf("-Wl,-install_name,@rpath/$MACOS_LIBRARY")
                        else -> emptyList()
                    }
                    links += {
                        this.runZig(listOf("cc", TARGET_OPTION, target, "-shared", "-s") + linkFlags +
                            listOf(OUTPUT_OPTION, library.absolutePath) + objects)
                        library.parentFile.listFiles()!!.filter { it.extension == IMPORT_LIBRARY_EXTENSION }.forEach { it.delete() }
                    }
                    libraries.add(library)
                }
            }
            this.inParallel(compiles)
            this.inParallel(links)
            this.writeManifest(natives.resolve(CHECKSUMS_MANIFEST), natives, libraries)
            val sources: File = this.parameters.sourcesDirectory.get().asFile
            this.writeManifest(natives.resolve(SOURCES_MANIFEST), sources, sources.listFiles()!!.filter { it.name != CLANG_FORMAT_FILE })
        }

        private fun inParallel(steps: List<() -> Unit>): Unit {
            val pool: ExecutorService = Executors.newFixedThreadPool(minOf(this.parameters.processCount.get(), steps.size))
            try {
                pool.invokeAll(steps.map { step -> Callable(step) }).forEach { result ->
                    try {
                        result.get()
                    } catch (exception: ExecutionException) {
                        val message: String = "Zig could not build MCV2's natives!"
                        throw GradleException(message, exception.cause)
                    }
                }
            } finally {
                pool.shutdown()
            }
        }

        private fun runZig(arguments: List<String>): Unit {
            val sources: File = this.parameters.sourcesDirectory.get().asFile
            val zig: String = this.parameters.zigExecutable.get().asFile.absolutePath
            val cache: String = this.parameters.cacheDirectory.get().asFile.absolutePath
            this.processes.exec {
                this.workingDir(sources)
                this.commandLine(listOf(zig) + arguments)
                this.environment(SOURCE_DATE_VARIABLE, REPRODUCIBLE_SOURCE_DATE)
                this.environment(ZIG_CACHE_VARIABLE, cache)
            }
        }

        private fun writeManifest(manifest: File, root: File, files: List<File>): Unit {
            manifest.writeText(files.sortedBy { it.relativeTo(root).invariantSeparatorsPath }.joinToString("", transform = { file ->
                val digest: String = ToolDigests.sha256(file)
                "$digest$MANIFEST_SEPARATOR${file.relativeTo(root).invariantSeparatorsPath}$LINE_SEPARATOR"
            }))
        }
    }
}
