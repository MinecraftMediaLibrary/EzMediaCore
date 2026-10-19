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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "The lock is resolved from the package index into the source tree")
abstract class LockPythonRequirements : DefaultTask() {

    companion object {

        private const val REQUIREMENTS_INPUT: String = "requirements.in"
        private const val LINE_SEPARATOR: String = "\n"
        private const val MARKER_SEPARATOR: String = " ; "
        private const val VERSION_SEPARATOR: Char = '.'

        fun requirementLines(versions: Map<String, String>, markers: Map<String, String>): List<String> {
            val sorted: Map<String, String> = versions.toSortedMap()
            return sorted.map { (name, version) -> this.requirementLine(name, version, markers[name]) }
        }

        private fun requirementLine(name: String, version: String, marker: String?): String {
            val pin: String = "$name${RequirementsLock.PIN_SEPARATOR}$version"
            return listOfNotNull(pin, marker).joinToString(MARKER_SEPARATOR)
        }
    }

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val uvExecutable: RegularFileProperty

    @get:Input
    abstract val uvVersion: Property<String>

    @get:Input
    abstract val pythonVersion: Property<String>

    @get:Input
    abstract val versions: MapProperty<String, String>

    @get:Input
    abstract val markers: MapProperty<String, String>

    @get:Input
    abstract val licenseHeader: Property<String>

    @get:OutputFile
    abstract val lockFile: RegularFileProperty

    @get:Internal
    abstract val installationDirectory: DirectoryProperty

    @get:Internal
    abstract val cacheDirectory: DirectoryProperty

    @get:Inject
    protected abstract val processes: ExecOperations

    @TaskAction
    fun lock(): Unit {
        val uv: File = this.uvExecutable.get().asFile
        val version: String = this.uvVersion.get()
        ToolVersions.verify(this.processes, uv, UvEnvironment.TOOL, version)

        val requirementsInput: File = this.writeRequirementsInput()
        val lock: File = this.lockFile.get().asFile
        this.compile(uv, requirementsInput, lock)

        val header: String = this.licenseHeader.get()
        lock.writeText(header + lock.readText())
    }

    private fun writeRequirementsInput(): File {
        val requirementsInput: File = this.temporaryDir.resolve(REQUIREMENTS_INPUT)
        val requirements: List<String> = requirementLines(this.versions.get(), this.markers.get())
        val text: String = requirements.joinToString(LINE_SEPARATOR, postfix = LINE_SEPARATOR)
        requirementsInput.writeText(text)

        return requirementsInput
    }

    private fun compile(uv: File, requirementsInput: File, lock: File): Unit {
        val python: String = this.pythonVersion.get()
        val targetVersion: String = python.substringBeforeLast(VERSION_SEPARATOR)
        val installation: File = this.installationDirectory.get().asFile
        val cache: File = this.cacheDirectory.get().asFile
        val environment: Map<String, String> = UvEnvironment.variables(installation, cache)
        this.processes.exec {
            this.commandLine(uv, "pip", "compile", requirementsInput, "--universal", "--generate-hashes", "--no-header", "--no-annotate",
                "--python", python, "--python-version", targetVersion, "--only-binary", ":all:",
                "--default-index", UvEnvironment.PACKAGE_INDEX, "--output-file", lock, "--quiet")
            this.environment(environment)
        }
    }
}
