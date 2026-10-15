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
import org.gradle.api.GradleException
import org.gradle.process.ExecOperations
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class PythonEnvironmentTest {

    @TempDir
    lateinit var directory: Path

    abstract class RecordingEnvironment : PythonEnvironment() {
        lateinit var recordedProcesses: ExecOperations
        override val processes: ExecOperations get() = recordedProcesses
    }

    @Test
    fun shouldInstallThePinnedPythonIntoAFreshVirtualEnvironmentAndSyncItWithTheHashedLock() {
        val task = environment("numpy==2.5.3 \\\n    --hash=sha256:00\npytest==9.1.1 \\\n    --hash=sha256:11\n")
        val processes = RecordedProcesses("uv 0.12.23\n")
        task.recordedProcesses = processes.operations
        task.install()
        val uv = directory.resolve("uv").toString()
        val installation = directory.resolve("python").toFile()
        val virtualEnvironment = installation.resolve("venv")
        val commands: List<List<String>> = processes.commands
        assertEquals(
            listOf(
                listOf(uv, "--version"),
                listOf(uv, "python", "install", "3.13.16", "--no-bin", "--no-registry"),
                listOf(uv, "venv", "--clear", "--no-project", "--no-python-downloads", "--python", "3.13.16", virtualEnvironment.path),
                listOf(uv, "pip", "sync", "--python", virtualEnvironment.resolve("bin/python").path, "--require-hashes",
                    "--only-binary", ":all:", "--default-index", "https://pypi.org/simple",
                    directory.resolve("requirements.lock").toString())
            ),
            commands
        )

        val uvVariables = uvEnvironment(installation, directory.resolve("uv-cache").toFile())
        val environments: List<Map<String, Any?>> = processes.environments
        assertEquals(listOf(emptyMap(), uvVariables, uvVariables, uvVariables), environments)

        val clangFormat: File = task.executable("clang-format").get().asFile
        assertEquals(virtualEnvironment.resolve("bin/clang-format"), clangFormat)
    }

    @Test
    fun shouldNameTheTaskThatWritesALockWithoutTheCatalogVersionsAndInstallNothing() {
        val task = environment("numpy==2.5.2 \\\n    --hash=sha256:00\n")
        val processes = RecordedProcesses("uv 0.12.23\n")
        task.recordedProcesses = processes.operations
        val failure = assertThrows(GradleException::class.java) { task.install() }
        val message: String? = failure.message
        assertEquals(
            "${directory.resolve("requirements.lock")} does not pin numpy==2.5.3, pytest==9.1.1 of gradle/libs.versions.toml; " +
                "run ./gradlew :lockPythonRequirements",
            message
        )

        val commands: List<List<String>> = processes.commands
        assertEquals(emptyList<List<String>>(), commands)
    }

    private fun environment(lock: String): RecordingEnvironment {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val task = project.tasks.register("installPythonEnvironment", RecordingEnvironment::class.java).get()
        val requirements = directory.resolve("requirements.lock").toFile()
        requirements.writeText(lock)
        task.uvExecutable.set(directory.resolve("uv").toFile())
        task.uvVersion.set("0.12.23")
        task.pythonVersion.set("3.13.16")
        task.requirements.set(requirements)
        task.versions.set(mapOf("numpy" to "2.5.3", "pytest" to "9.1.1"))
        task.lockTask.set(":lockPythonRequirements")
        task.installationDirectory.set(directory.resolve("python").toFile())
        task.cacheDirectory.set(directory.resolve("uv-cache").toFile())
        return task
    }
}
