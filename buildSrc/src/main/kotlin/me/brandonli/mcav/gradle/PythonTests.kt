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
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

@CacheableTask
abstract class PythonTests : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val parallelTests: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val serialTests: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val requirements: RegularFileProperty

    @get:Input
    abstract val pythonVersion: Property<String>

    @get:Input
    abstract val platform: Property<String>

    @get:Internal
    abstract val pythonExecutable: RegularFileProperty

    @get:Internal
    abstract val testDirectory: DirectoryProperty

    @get:Internal
    abstract val processCount: Property<Int>

    @get:OutputDirectory
    abstract val reportDirectory: DirectoryProperty

    @get:Inject
    protected abstract val files: FileSystemOperations

    @get:Inject
    protected abstract val processes: ExecOperations

    @TaskAction
    fun test() {
        val reports = reportDirectory.get().asFile
        files.delete { delete(reports) }
        val parallel = parallelTests.files.sorted()
        pytest(reports.resolve("parallel.xml"), listOf("-n", minOf(processCount.get(), parallel.size).toString(), "--dist", "loadfile") +
            parallel.map { it.path })
        pytest(reports.resolve("serial.xml"), serialTests.files.sorted().map { it.path })
    }

    private fun pytest(report: File, arguments: List<String>) {
        val directory = testDirectory.get().asFile
        processes.exec {
            workingDir(directory)
            commandLine(listOf(pythonExecutable.get().asFile.path, "-m", "pytest", "-p", "no:cacheprovider", "-ra", "--rootdir", directory.path,
                "--junitxml", report.path) + arguments)
            environment("PYTHONNOUSERSITE", "1")
            environment("PYTHONDONTWRITEBYTECODE", "1")
            environment("PYTHONHASHSEED", "0")
            environment("PYTEST_ADDOPTS", "")
            environment("MPLBACKEND", "Agg")
            environment("MPLCONFIGDIR", temporaryDir.resolve("matplotlib").path)
        }
    }
}
