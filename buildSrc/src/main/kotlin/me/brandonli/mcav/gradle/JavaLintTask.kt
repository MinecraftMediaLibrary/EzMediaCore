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
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class JavaLintTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun check(): Unit {
        val files: List<File> = this.sources.files.sorted()
        val violations: MutableList<List<Any>> = mutableListOf<List<Any>>()
        this.findViolations(files) { file, line, arguments -> violations += listOf(file, line) + arguments }
        violations.forEach(this::logViolation)

        if (violations.isNotEmpty()) {
            val message: String = this.failureMessage(violations.size)
            throw GradleException(message)
        }

        val success: String = this.successMessage(files.size)
        val report: File = this.report.get().asFile
        report.writeText(success)
    }

    private fun logViolation(logArguments: List<Any>): Unit {
        val diagnostic: String = this.diagnosticMessage()
        this.logger.error(diagnostic, *logArguments.toTypedArray())
    }

    protected abstract fun findViolations(files: List<File>, reportViolation: (File, Number, List<String>) -> Unit): Unit

    protected abstract fun diagnosticMessage(): String

    protected abstract fun failureMessage(count: Int): String

    protected abstract fun successMessage(fileCount: Int): String
}
