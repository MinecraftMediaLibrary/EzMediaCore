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
import org.gradle.api.GradleException
import org.gradle.process.ExecOperations
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class LockPythonRequirementsTest {

    @TempDir
    lateinit var directory: Path

    abstract class RecordingLock : LockPythonRequirements() {
        lateinit var recordedProcesses: ExecOperations
        override val processes: ExecOperations get() = recordedProcesses
    }

    @Test
    fun requirementsAreSortedByNameAndKeepTheMarkersOfPackagesWithoutWheelsEverywhere() {
        val marker = "sys_platform != 'win32' or platform_machine != 'ARM64'"
        assertEquals(
            listOf("clang-format==18.1.8 ; $marker", "numpy==2.5.3", "pytest==9.1.1"),
            LockPythonRequirements.requirementLines(
                mapOf("pytest" to "9.1.1", "numpy" to "2.5.3", "clang-format" to "18.1.8"),
                mapOf("clang-format" to marker)
            )
        )
    }

    @Test
    fun compilesAUniversalHashedLockOfWheelsForThePythonMinorBelowTheLicenseHeader() {
        val task = lockTask()
        val lock = directory.resolve("requirements.lock").toFile()
        val requirementsInput = task.temporaryDir.resolve("requirements.in")
        val processes = RecordedProcesses("uv 0.12.23\n") { command ->
            if (command[1] == "pip") {
                assertEquals("clang-format==18.1.8 ; platform_machine != 'ARM64'\nnumpy==2.5.3\n", requirementsInput.readText())
                lock.writeText("numpy==2.5.3 \\\n    --hash=sha256:00\n")
            }
        }
        task.recordedProcesses = processes.operations
        task.lock()
        val uv = directory.resolve("uv").toString()
        assertEquals(
            listOf(
                listOf(uv, "--version"),
                listOf(uv, "pip", "compile", requirementsInput.path, "--universal", "--generate-hashes", "--no-header",
                    "--no-annotate", "--python", "3.13.16", "--python-version", "3.13", "--only-binary", ":all:",
                    "--default-index", "https://pypi.org/simple", "--output-file", lock.path, "--quiet")
            ),
            processes.commands
        )
        assertEquals(uvEnvironment(directory.resolve("python").toFile(), directory.resolve("uv-cache").toFile()), processes.environments[1])
        assertEquals("# License header\n\nnumpy==2.5.3 \\\n    --hash=sha256:00\n", lock.readText())
    }

    @Test
    fun anUvOfAnotherVersionLeavesTheLockAsItIs() {
        val task = lockTask()
        val lock = directory.resolve("requirements.lock").toFile()
        lock.writeText("numpy==2.5.2\n")
        val processes = RecordedProcesses("uv 0.12.22\n")
        task.recordedProcesses = processes.operations
        val failure = assertThrows(GradleException::class.java) { task.lock() }
        assertEquals("${directory.resolve("uv")} must be uv 0.12.23, but it is uv 0.12.22", failure.message)
        assertEquals(1, processes.commands.size)
        assertEquals("numpy==2.5.2\n", lock.readText())
    }

    private fun lockTask(): RecordingLock {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val task = project.tasks.register("lockPythonRequirements", RecordingLock::class.java).get()
        task.uvExecutable.set(directory.resolve("uv").toFile())
        task.uvVersion.set("0.12.23")
        task.pythonVersion.set("3.13.16")
        task.versions.set(mapOf("numpy" to "2.5.3", "clang-format" to "18.1.8"))
        task.markers.set(mapOf("clang-format" to "platform_machine != 'ARM64'"))
        task.licenseHeader.set("# License header\n\n")
        task.lockFile.set(directory.resolve("requirements.lock").toFile())
        task.installationDirectory.set(directory.resolve("python").toFile())
        task.cacheDirectory.set(directory.resolve("uv-cache").toFile())
        return task
    }
}
