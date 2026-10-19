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
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class PythonTestsTest {

    private companion object {

        const val WORKERS_OPTION: String = "-n"

        const val TESTS_DIRECTORY: String = "python"
        const val FIRST_TEST: String = "test_a.py"
        const val SECOND_TEST: String = "test_b.py"
        const val THIRD_TEST: String = "test_c.py"
        const val TIMING_TEST: String = "test_timing.py"

        const val REPORTS_DIRECTORY: String = "reports"
        const val STALE_REPORT: String = "stale.xml"
        const val PARALLEL_REPORT: String = "parallel.xml"
        const val SERIAL_REPORT: String = "serial.xml"

        const val VIRTUAL_ENVIRONMENT_PYTHON: String = "venv/bin/python"

        const val MATPLOTLIB_DIRECTORY: String = "matplotlib"
    }

    @TempDir
    lateinit var directory: Path

    abstract class RecordingTests : PythonTests() {

        lateinit var recordedProcesses: ExecOperations
        override val processes: ExecOperations get() = this.recordedProcesses
    }

    @Test
    fun shouldRunTheTestFilesInOneProcessEachAndTheTimingTestsAfterThemInASingleProcess(): Unit {
        val task: RecordingTests = this.pythonTests(8)
        val reports: File = this.directory.resolve(REPORTS_DIRECTORY).toFile()
        reports.mkdirs()
        reports.resolve(STALE_REPORT).writeText("<testsuites/>")
        val processes: RecordedProcesses = RecordedProcesses("") { this.assertNoStaleReport(reports) }
        task.recordedProcesses = processes.operations
        task.test()
        val tests: Path = this.directory.resolve(TESTS_DIRECTORY)
        val commands: List<List<String>> = processes.commands
        assertEquals(
            listOf(
                this.pytest(PARALLEL_REPORT) + listOf(WORKERS_OPTION, "3", "--dist", "loadfile") +
                    listOf(FIRST_TEST, SECOND_TEST, THIRD_TEST).map { tests.resolve(it).toString() },
                this.pytest(SERIAL_REPORT) + tests.resolve(TIMING_TEST).toString()
            ),
            commands
        )

        val workingDirectories: List<Any?> = processes.workingDirectories
        assertEquals(listOf(tests.toFile(), tests.toFile()), workingDirectories)

        val variables: Map<String, String> = mapOf(
            "PYTHONNOUSERSITE" to "1",
            "PYTHONDONTWRITEBYTECODE" to "1",
            "PYTHONHASHSEED" to "0",
            "PYTEST_ADDOPTS" to "",
            "MPLBACKEND" to "Agg",
            "MPLCONFIGDIR" to task.temporaryDir.resolve(MATPLOTLIB_DIRECTORY).path
        )
        val environments: List<Map<String, Any?>> = processes.environments
        assertEquals(listOf(variables, variables), environments)
    }

    @Test
    fun shouldStartNoMoreProcessesThanGradleMayUse(): Unit {
        val task: RecordingTests = this.pythonTests(2)
        val processes: RecordedProcesses = RecordedProcesses("")
        task.recordedProcesses = processes.operations
        task.test()
        val parallel: List<String> = processes.commands.first()
        val processCount: String = parallel[parallel.indexOf(WORKERS_OPTION) + 1]
        assertEquals("2", processCount)
    }

    private fun assertNoStaleReport(reports: File): Unit {
        val stale: Boolean = reports.resolve(STALE_REPORT).exists()
        assertFalse(stale)
    }

    private fun pytest(report: String): List<String> = listOf(
        this.directory.resolve(VIRTUAL_ENVIRONMENT_PYTHON).toString(),
        "-m",
        "pytest",
        "-p",
        "no:cacheprovider",
        "-ra",
        "--rootdir",
        this.directory.resolve(TESTS_DIRECTORY).toString(),
        "--junitxml",
        this.directory.resolve(REPORTS_DIRECTORY).resolve(report).toString()
    )

    private fun pythonTests(processCount: Int): RecordingTests {
        val project: Project = ProjectBuilder.builder().withProjectDir(this.directory.toFile()).build()
        val tests: File = this.directory.resolve(TESTS_DIRECTORY).toFile()
        tests.mkdirs()
        val parallel: List<File> = listOf(SECOND_TEST, FIRST_TEST, THIRD_TEST).map { tests.resolve(it) }
        val timing: File = tests.resolve(TIMING_TEST)
        (parallel + timing).forEach { it.writeText("") }
        val task: RecordingTests = project.tasks.register("pythonTest", RecordingTests::class.java).get()
        task.pythonExecutable.set(this.directory.resolve(VIRTUAL_ENVIRONMENT_PYTHON).toFile())
        task.testDirectory.set(tests)
        task.parallelTests.from(parallel)
        task.serialTests.from(timing)
        task.processCount.set(processCount)
        task.reportDirectory.set(this.directory.resolve(REPORTS_DIRECTORY).toFile())
        return task
    }
}
