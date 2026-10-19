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
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.LocalState
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Python virtual environments contain absolute installation paths")
abstract class PythonEnvironment : DefaultTask() {

    private companion object {

        const val VIRTUAL_ENVIRONMENT_DIRECTORY: String = "venv"
        const val PATH_SEPARATOR: String = "/"
    }

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val uvExecutable: RegularFileProperty

    @get:Input
    abstract val uvVersion: Property<String>

    @get:Input
    abstract val pythonVersion: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val requirements: RegularFileProperty

    @get:Input
    abstract val versions: MapProperty<String, String>

    @get:Internal
    abstract val lockTask: Property<String>

    @get:OutputDirectory
    abstract val installationDirectory: DirectoryProperty

    @get:LocalState
    abstract val cacheDirectory: DirectoryProperty

    @get:Inject
    protected abstract val processes: ExecOperations

    fun executable(name: String): Provider<RegularFile> {
        val executablePath: String = HostPlatform.current().virtualEnvironmentExecutable(name)
        return this.installationDirectory.file("$VIRTUAL_ENVIRONMENT_DIRECTORY$PATH_SEPARATOR$executablePath")
    }

    @TaskAction
    fun install(): Unit {
        val lock: File = this.requirements.get().asFile
        val missing: List<String> = RequirementsLock.missingPins(lock.readText(), this.versions.get())
        if (missing.isNotEmpty()) {
            val lockTaskPath: String = this.lockTask.get()
            val message: String =
                "$lock does not pin ${missing.joinToString()} of gradle/libs.versions.toml, so run ./gradlew $lockTaskPath to write it!"
            throw GradleException(message)
        }

        val uv: File = this.uvExecutable.get().asFile
        val version: String = this.uvVersion.get()
        ToolVersions.verify(this.processes, uv, UvEnvironment.TOOL, version)

        val python: String = this.pythonVersion.get()
        val virtualEnvironment: File = this.installationDirectory.get().asFile.resolve(VIRTUAL_ENVIRONMENT_DIRECTORY)
        this.runUv(uv, listOf("python", "install", python, "--no-bin", "--no-registry"))
        this.runUv(uv, listOf("venv", "--clear", "--no-project", "--no-python-downloads", "--python", python, virtualEnvironment))
        this.syncPackages(uv, virtualEnvironment, lock)
    }

    private fun syncPackages(uv: File, virtualEnvironment: File, lock: File): Unit {
        val pythonPath: String = HostPlatform.current().virtualEnvironmentExecutable(PythonTask.PYTHON_EXECUTABLE)
        val virtualEnvironmentPython: File = virtualEnvironment.resolve(pythonPath)
        val arguments: List<Any> = listOf(
            "pip", "sync", "--python", virtualEnvironmentPython, "--require-hashes",
            "--only-binary", ":all:", "--default-index", UvEnvironment.PACKAGE_INDEX, lock
        )
        this.runUv(uv, arguments)
    }

    private fun runUv(uv: File, arguments: List<Any>): Unit {
        val installation: File = this.installationDirectory.get().asFile
        val cache: File = this.cacheDirectory.get().asFile
        val environment: Map<String, String> = UvEnvironment.variables(installation, cache)
        this.processes.exec {
            this.commandLine(listOf(uv) + arguments)
            this.environment(environment)
        }
    }
}
