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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class CoverageLintTask : DefaultTask() {

    private companion object {

        const val FILTERED: String =
            "Skipped coverageLint of {}, since the tests ran with a filter and the report covers only part of the code."
        const val GAP: String = "{}"
        const val STALE_EXCEPTION: String = "{}: warning: '{} | {}' matches no uncovered line, so remove it."
    }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val report: ConfigurableFileCollection

    @get:Internal
    abstract val sourceDirectory: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val exceptionsFile: ConfigurableFileCollection

    @get:Input
    abstract val testsFiltered: Property<Boolean>

    @get:Internal
    abstract val projectPath: Property<String>

    @TaskAction
    fun lint(): Unit {
        if (this.testsFiltered.get()) {
            this.logger.warn(FILTERED, this.projectPath.get())
            return
        }
        val exceptionFile: File = this.exceptionsFile.singleFile
        val exceptions: List<CoverageException> = CoverageExceptions.read(exceptionFile)
        val sources: File = this.sourceDirectory.get().asFile
        val gaps: List<String> = CoverageReport.findGaps(this.report.singleFile, sources, exceptions)
        gaps.forEach { gap -> this.logger.error(GAP, gap) }
        val staleExceptions: List<CoverageException> = exceptions.filterNot { it.used }
        staleExceptions.forEach { exception ->
            this.logger.error(STALE_EXCEPTION, exceptionFile, exception.path, exception.sourceLine)
        }
        if (gaps.isNotEmpty() || staleExceptions.isNotEmpty()) {
            val message: String =
                "${gaps.size} lines are not fully covered by tests and ${staleExceptions.size} coverage exceptions are stale!"
            throw GradleException(message)
        }
    }
}
