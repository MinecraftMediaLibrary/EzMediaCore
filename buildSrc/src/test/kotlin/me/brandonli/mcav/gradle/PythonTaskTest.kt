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
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class PythonTaskTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldRunATaskThatUsesTheEnvironmentAfterItOnItsLockPythonAndHost() {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val environment = project.tasks.register("installPythonEnvironment", PythonEnvironment::class.java)
        environment.get().requirements.set(directory.resolve("requirements.lock").toFile())
        environment.get().pythonVersion.set("3.13.16")
        environment.get().installationDirectory.set(directory.resolve("python").toFile())
        val book = project.tasks.register("book", JupyterBook::class.java).get()
        book.useEnvironment(environment)
        val dependencies: Set<Task> = book.taskDependencies.getDependencies(book)
        assertEquals(setOf(environment.get()), dependencies)

        val requirements: File = book.requirements.get().asFile
        assertEquals(directory.resolve("requirements.lock").toFile(), requirements)

        val pythonVersion: String = book.pythonVersion.get()
        assertEquals("3.13.16", pythonVersion)

        val platform: String = book.platform.get()
        assertEquals(HostPlatform.current().uvTarget, platform)

        val pythonExecutable: File = book.pythonExecutable.get().asFile
        assertEquals(directory.resolve("python/venv").resolve(virtualEnvironmentExecutable("python")).toFile(), pythonExecutable)
    }
}
