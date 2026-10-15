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

internal const val UV_TOOL: String = "uv"
internal const val PACKAGE_INDEX: String = "https://pypi.org/simple"

private const val VIRTUAL_ENVIRONMENT_DIRECTORY: String = "venv"
private const val PYTHON_DIRECTORY: String = "python"
private const val BINARY_DIRECTORY: String = "bin"
private const val PATH_SEPARATOR: String = "/"

private const val NO_CONFIG_VARIABLE: String = "UV_NO_CONFIG"
private const val CACHE_VARIABLE: String = "UV_CACHE_DIR"
private const val PYTHON_CACHE_VARIABLE: String = "UV_PYTHON_CACHE_DIR"
private const val PYTHON_INSTALL_VARIABLE: String = "UV_PYTHON_INSTALL_DIR"
private const val PYTHON_BINARY_VARIABLE: String = "UV_PYTHON_BIN_DIR"
private const val PYTHON_PREFERENCE_VARIABLE: String = "UV_PYTHON_PREFERENCE"
private const val LINK_MODE_VARIABLE: String = "UV_LINK_MODE"

private const val ENABLED: String = "1"
private const val MANAGED_PYTHON_ONLY: String = "only-managed"
private const val COPY_LINKS: String = "copy"

fun uvEnvironment(installationDirectory: File, cacheDirectory: File): Map<String, String> = mapOf(
    NO_CONFIG_VARIABLE to ENABLED,
    CACHE_VARIABLE to cacheDirectory.absolutePath,
    PYTHON_CACHE_VARIABLE to cacheDirectory.resolve(PYTHON_DIRECTORY).absolutePath,
    PYTHON_INSTALL_VARIABLE to installationDirectory.resolve(PYTHON_DIRECTORY).absolutePath,
    PYTHON_BINARY_VARIABLE to installationDirectory.resolve(BINARY_DIRECTORY).absolutePath,
    PYTHON_PREFERENCE_VARIABLE to MANAGED_PYTHON_ONLY,
    LINK_MODE_VARIABLE to COPY_LINKS
)

@DisableCachingByDefault(because = "Python virtual environments contain absolute installation paths")
abstract class PythonEnvironment : DefaultTask() {

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

    fun executable(name: String): Provider<RegularFile> =
        installationDirectory.file(VIRTUAL_ENVIRONMENT_DIRECTORY + PATH_SEPARATOR + virtualEnvironmentExecutable(name))

    @TaskAction
    fun install() {
        val lock = requirements.get().asFile
        val missing = RequirementsLock.missingPins(lock.readText(), versions.get())
        if (missing.isNotEmpty()) {
            val message =
                "$lock does not pin ${missing.joinToString()} of gradle/libs.versions.toml, so run ./gradlew ${lockTask.get()} to write it!"
            throw GradleException(message)
        }
        val uv = uvExecutable.get().asFile
        ToolVersions.verify(processes, uv, UV_TOOL, uvVersion.get())
        val installation = installationDirectory.get().asFile
        val environment = uvEnvironment(installation, cacheDirectory.get().asFile)
        val virtualEnvironment = installation.resolve(VIRTUAL_ENVIRONMENT_DIRECTORY)
        val python = pythonVersion.get()
        val virtualEnvironmentPython: File = virtualEnvironment.resolve(virtualEnvironmentExecutable(PythonTask.PYTHON_EXECUTABLE))
        listOf(
            listOf("python", "install", python, "--no-bin", "--no-registry"),
            listOf("venv", "--clear", "--no-project", "--no-python-downloads", "--python", python, virtualEnvironment),
            listOf("pip", "sync", "--python", virtualEnvironmentPython, "--require-hashes",
                "--only-binary", ":all:", "--default-index", PACKAGE_INDEX, lock)
        ).forEach { arguments ->
            processes.exec {
                commandLine(listOf(uv) + arguments)
                environment(environment)
            }
        }
    }
}
