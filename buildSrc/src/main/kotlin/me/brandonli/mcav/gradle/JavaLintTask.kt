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

/**
 * A lint of Java sources that logs every violation it finds and fails the build when there is one.
 *
 * Subclasses find the violations and word the messages. When the sources are clean, the task writes the success message
 * to [report], so that the lint stays up to date until a source changes.
 */
abstract class JavaLintTask : DefaultTask() {

    /**
     * The Java sources to check.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    /**
     * The report file that receives the success message.
     */
    @get:OutputFile
    abstract val report: RegularFileProperty

    /**
     * Checks every source, logs each violation and writes the report when there is none.
     *
     * @throws GradleException if a source has a violation
     */
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

    /**
     * Finds the violations in [files] and reports each one to [reportViolation].
     *
     * @param files           the sources to check, sorted by path
     * @param reportViolation receives the file, the line and the other arguments of the diagnostic message of a violation
     */
    protected abstract fun findViolations(files: List<File>, reportViolation: (File, Number, List<String>) -> Unit): Unit

    /**
     * Returns the SLF4J template that logs one violation, with the file and the line as its first two arguments.
     *
     * @return the template
     */
    protected abstract fun diagnosticMessage(): String

    /**
     * Returns the message of the build failure.
     *
     * @param count the number of violations
     * @return the message
     */
    protected abstract fun failureMessage(count: Int): String

    /**
     * Returns the report text of clean sources.
     *
     * @param fileCount the number of checked files
     * @return the report text
     */
    protected abstract fun successMessage(fileCount: Int): String
}
