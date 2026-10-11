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
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class PythonTaskTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun aTaskUsingTheEnvironmentRunsAfterItOnItsLockPythonAndHost() {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val environment = project.tasks.register("installPythonEnvironment", PythonEnvironment::class.java)
        environment.get().requirements.set(directory.resolve("requirements.lock").toFile())
        environment.get().pythonVersion.set("3.13.16")
        environment.get().installationDirectory.set(directory.resolve("python").toFile())
        val book = project.tasks.register("book", JupyterBook::class.java).get()
        book.useEnvironment(environment)
        assertEquals(setOf(environment.get()), book.taskDependencies.getDependencies(book))
        assertEquals(directory.resolve("requirements.lock").toFile(), book.requirements.get().asFile)
        assertEquals("3.13.16", book.pythonVersion.get())
        assertEquals(HostPlatform.current().uvTarget, book.platform.get())
        assertEquals(directory.resolve("python/venv").resolve(venvExecutable("python")).toFile(), book.pythonExecutable.get().asFile)
    }
}
