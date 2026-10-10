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

import java.nio.file.Path
import org.gradle.process.ExecOperations
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class PythonTestsTest {

    @TempDir
    lateinit var directory: Path

    abstract class RecordingTests : PythonTests() {
        lateinit var recordedProcesses: ExecOperations
        override val processes: ExecOperations get() = recordedProcesses
    }

    @Test
    fun runsTheTestFilesInOneProcessEachAndTheTimingTestsAfterThemInASingleProcess() {
        val task = pythonTests(8)
        val reports = directory.resolve("reports").toFile()
        reports.mkdirs()
        reports.resolve("stale.xml").writeText("<testsuites/>")
        val processes = RecordedProcesses("") { assertFalse(reports.resolve("stale.xml").exists()) }
        task.recordedProcesses = processes.operations
        task.test()
        val tests = directory.resolve("python")
        assertEquals(
            listOf(
                pytest("parallel.xml") + listOf("-n", "3", "--dist", "loadfile") +
                    listOf("test_a.py", "test_b.py", "test_c.py").map { tests.resolve(it).toString() },
                pytest("serial.xml") + tests.resolve("test_timing.py").toString()
            ),
            processes.commands
        )
        assertEquals(listOf(tests.toFile(), tests.toFile()), processes.workingDirectories)
        val variables = mapOf(
            "PYTHONNOUSERSITE" to "1",
            "PYTHONDONTWRITEBYTECODE" to "1",
            "PYTHONHASHSEED" to "0",
            "PYTEST_ADDOPTS" to "",
            "MPLBACKEND" to "Agg",
            "MPLCONFIGDIR" to task.temporaryDir.resolve("matplotlib").path
        )
        assertEquals(listOf(variables, variables), processes.environments)
    }

    @Test
    fun startsNoMoreProcessesThanGradleMayUse() {
        val task = pythonTests(2)
        val processes = RecordedProcesses("")
        task.recordedProcesses = processes.operations
        task.test()
        val parallel = processes.commands.first()
        assertEquals("2", parallel[parallel.indexOf("-n") + 1])
    }

    private fun pytest(report: String): List<String> = listOf(
        directory.resolve("venv/bin/python").toString(),
        "-m",
        "pytest",
        "-p",
        "no:cacheprovider",
        "-ra",
        "--rootdir",
        directory.resolve("python").toString(),
        "--junitxml",
        directory.resolve("reports").resolve(report).toString()
    )

    private fun pythonTests(processCount: Int): RecordingTests {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val tests = directory.resolve("python").toFile()
        tests.mkdirs()
        val parallel = listOf("test_b.py", "test_a.py", "test_c.py").map { tests.resolve(it) }
        val timing = tests.resolve("test_timing.py")
        (parallel + timing).forEach { it.writeText("") }
        val task = project.tasks.register("pythonTest", RecordingTests::class.java).get()
        task.pythonExecutable.set(directory.resolve("venv/bin/python").toFile())
        task.testDirectory.set(tests)
        task.parallelTests.from(parallel)
        task.serialTests.from(timing)
        task.processCount.set(processCount)
        task.reportDirectory.set(directory.resolve("reports").toFile())
        return task
    }
}
