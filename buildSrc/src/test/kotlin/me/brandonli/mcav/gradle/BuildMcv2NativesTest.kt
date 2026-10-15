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
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import me.brandonli.mcav.gradle.BuildMcv2Natives.Work.Parameters
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.process.ExecOperations
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class BuildMcv2NativesTest {

    @TempDir
    lateinit var directory: Path

    abstract class RecordingBuild : BuildMcv2Natives() {
        lateinit var recordedProcesses: ExecOperations
        lateinit var recordedWorkers: WorkerExecutor
        override val processes: ExecOperations get() = recordedProcesses
        override val workers: WorkerExecutor get() = recordedWorkers
    }

    class RecordingWork(private val recordedParameters: Parameters, override val processes: ExecOperations) : BuildMcv2Natives.Work() {
        override fun getParameters() = recordedParameters
    }

    private lateinit var processes: RecordedProcesses

    private val commands get() = processes.commands.map { it.drop(1) }

    private val submitted = mutableListOf<RecordingWork>()

    @Test
    fun shouldHandTheCompilesAndLinksToAGradleWorker() {
        val task = compiler("test-version")
        task.build()
        assertEquals(listOf(listOf("version")), commands)

        val staleBeforeWork: Boolean = task.outputDirectory.get().file("stale.dll").asFile.exists()
        assertTrue(staleBeforeWork)

        submitted.single().execute()
        val compileCount: Int = commands.count { it.first() == "c++" }
        assertEquals(22, compileCount)

        val linkCount: Int = commands.count { it.first() == "cc" }
        assertEquals(6, linkCount)

        val staleAfterWork: Boolean = task.outputDirectory.get().file("stale.dll").asFile.exists()
        assertFalse(staleAfterWork)
    }

    @Test
    fun shouldBuildAllTargetsWithTheirBaselineAndOnlyTheirOwnExtensions() {
        val task = compiler("test-version")
        buildWithWorkers(task)
        val links = commands.filter { it.first() == "cc" }
        val targets: Set<String> = links.map { it[2] }.toSet()
        assertEquals(setOf("x86_64-linux-gnu.2.28", "aarch64-linux-gnu.2.28", "x86_64-windows-gnu",
            "aarch64-windows-gnu", "x86_64-macos.11.0", "aarch64-macos.11.0"), targets)

        val linkCount: Int = links.size
        assertEquals(6, linkCount)

        val shared: Boolean = links.all { "-shared" in it && "-s" in it }
        assertTrue(shared)

        val linuxStacksNotExecutable: Boolean = links.filter { "linux" in it[2] }.all { "-Wl,-z,noexecstack" in it }
        assertTrue(linuxStacksNotExecutable)

        val macosInstallNamed: Boolean = links.filter { "macos" in it[2] }.all { "-Wl,-install_name,@rpath/libmcv2kernels.dylib" in it }
        assertTrue(macosInstallNamed)

        val compiles = commands.filter { it.first() == "c++" }
        val compileCount: Int = compiles.size
        assertEquals(22, compileCount)

        for (command in compiles) {
            assertCompileFlags(command)
        }

        val reproducible: Boolean = processes.environments.drop(1).all { it["SOURCE_DATE_EPOCH"] == "0" }
        assertTrue(reproducible)

        val natives = task.outputDirectory.get().asFile.resolve("mcav/mcv2/natives")
        val expectedPaths = setOf("linux-x86_64/libmcv2kernels.so", "linux-aarch64/libmcv2kernels.so",
            "macos-x86_64/libmcv2kernels.dylib", "macos-aarch64/libmcv2kernels.dylib",
            "windows-x86_64/mcv2kernels.dll", "windows-aarch64/mcv2kernels.dll")
        val sums = natives.resolve("SHA256SUMS").readLines()
        val summedPaths: Set<String> = sums.map { it.substringAfter("  ") }.toSet()
        assertEquals(expectedPaths, summedPaths)

        val summedContents: Boolean = sums.all { it.startsWith("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad  ") }
        assertTrue(summedContents)

        val sourceSums: String = natives.resolve("SOURCES").readText()
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad  mcv2.cpp\n", sourceSums)

        val staleKept: Boolean = task.outputDirectory.get().file("stale.dll").asFile.exists()
        assertFalse(staleKept)

        val importLibraryKept: Boolean = natives.walkTopDown().any { it.extension == "lib" }
        assertFalse(importLibraryKept)
    }

    @Test
    fun shouldCompileAsManyObjectsAtOnceAsItHasProcesses() {
        val bothStarted = CountDownLatch(2)
        val running = AtomicInteger()
        val mostAtOnce = AtomicInteger()
        val task = compiler("test-version", 2) {
            mostAtOnce.accumulateAndGet(running.incrementAndGet(), ::maxOf)
            bothStarted.countDown()
            bothStarted.await(30, TimeUnit.SECONDS)
            running.decrementAndGet()
        }
        buildWithWorkers(task)
        val mostCompilesAtOnce: Int = mostAtOnce.get()
        assertEquals(2, mostCompilesAtOnce)

        val compileCount: Int = commands.count { it.first() == "c++" }
        assertEquals(22, compileCount)
    }

    @Test
    fun shouldFailTheBuildOnAFailingCompileBeforeAnyLink() {
        val failure = IllegalStateException("zig: error: unable to compile")
        val task = compiler("test-version") { arguments ->
            if ("level_avx2.cpp" in arguments) {
                throw failure
            }
        }
        val thrown = assertThrows(GradleException::class.java) { buildWithWorkers(task) }
        val message: String? = thrown.message
        assertEquals("Zig could not build MCV2's natives!", message)

        val cause: Throwable? = thrown.cause
        assertSame(failure, cause)

        val linked: Boolean = commands.any { it.first() == "cc" }
        assertFalse(linked)
    }

    @Test
    fun shouldFailOnAWrongCompilerVersionBeforeReplacingAnyOutput() {
        val task = compiler("wrong-version")
        val failure = assertThrows(GradleException::class.java) { task.build() }
        val zig = directory.resolve("zig").toString()
        val message: String? = failure.message
        assertEquals("$zig reports wrong-version, which is not the zig test-version this build pins!", message)

        val staleKept: Boolean = task.outputDirectory.get().file("stale.dll").asFile.exists()
        assertTrue(staleKept)

        val recordedCommands: List<List<String>> = processes.commands
        assertEquals(listOf(listOf(zig, "version")), recordedCommands)

        val nothingSubmitted: Boolean = submitted.isEmpty()
        assertTrue(nothingSubmitted)
    }

    private fun assertCompileFlags(command: List<String>) {
        val baseline: Boolean = "-mcpu=baseline" in command
        assertTrue(baseline)

        val strict: Boolean = "-ffp-contract=off" in command && "-fwrapv" in command && "-fno-strict-aliasing" in command
        assertTrue(strict)

        val hostTuned: Boolean = command.any { "native" == it.substringAfter('=') || it == "-ffast-math" }
        assertFalse(hostTuned)

        val source = command[command.indexOf("-c") + 1]
        val avx2: Boolean = "-mavx2" in command
        assertEquals(source == "level_avx2.cpp", avx2)

        val avx512: Boolean = "-mavx512f" in command
        assertEquals(source == "level_avx512.cpp", avx512)

        val sve: Boolean = "-mcpu=baseline+sve" in command
        assertEquals(source.startsWith("level_sve"), sve)

        val branchesAligned: Boolean = "-mbranches-within-32B-boundaries" in command
        assertEquals(command[2].startsWith("x86_64") && "macos" !in command[2], branchesAligned)
    }

    private fun buildWithWorkers(task: RecordingBuild) {
        task.build()
        submitted.forEach { it.execute() }
    }

    private fun compiler(version: String, processCount: Int = 4, onCompile: (List<String>) -> Unit = {}): RecordingBuild {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val task = project.tasks.register("natives", RecordingBuild::class.java).get()
        task.zigVersion.set("test-version")
        task.zigExecutable.set(directory.resolve("zig").toFile())
        task.sourcesDirectory.set(directory.resolve("sources").toFile().apply { mkdirs() })
        task.sourcesDirectory.file("mcv2.cpp").get().asFile.writeText("abc")
        task.sourcesDirectory.file(".clang-format").get().asFile.writeText("formatter")
        task.outputDirectory.set(directory.resolve("output").toFile().apply { mkdirs() })
        task.outputDirectory.file("stale.dll").get().asFile.writeText("stale")
        task.cacheDirectory.set(directory.resolve("cache").toFile())
        task.processCount.set(processCount)
        processes = RecordedProcesses("$version\n") { command ->
            val arguments = command.drop(1)
            if (arguments != listOf("version")) {
                if (arguments.first() == "c++") {
                    onCompile(arguments)
                }
                val output = File(arguments[arguments.indexOf("-o") + 1])
                output.writeText("abc")
                if (output.extension == "dll") output.resolveSibling("mcv2kernels.lib").writeText("unused")
            }
        }
        task.recordedProcesses = processes.operations
        val queue = mock(WorkQueue::class.java)
        doAnswer { invocation ->
            val workClass: Class<*> = invocation.getArgument<Class<*>>(0)
            assertEquals(BuildMcv2Natives.Work::class.java, workClass)
            val parameters = project.objects.newInstance(Parameters::class.java)
            invocation.getArgument<Action<Parameters>>(1).execute(parameters)
            submitted.add(RecordingWork(parameters, processes.operations))
        }.`when`(queue).submit(any<Class<BuildMcv2Natives.Work>>(), any<Action<Parameters>>())
        val workers = mock(WorkerExecutor::class.java)
        `when`(workers.noIsolation()).thenReturn(queue)
        task.recordedWorkers = workers
        return task
    }
}
