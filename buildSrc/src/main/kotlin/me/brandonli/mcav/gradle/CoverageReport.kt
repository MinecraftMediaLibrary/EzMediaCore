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
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

class CoverageException(val path: String, val sourceLine: String, val reason: String) {
    var used = false
}

object CoverageExceptions {

    private const val SEPARATOR = " | "

    private const val COMMENT_PREFIX: String = "#"

    fun read(file: File): List<CoverageException> {
        if (!file.isFile) {
            return emptyList()
        }
        val entries = file.readLines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith(COMMENT_PREFIX) }
        return entries.map { entry -> parse(file, entry) }
    }

    private fun parse(file: File, entry: String): CoverageException {
        val first = entry.indexOf(SEPARATOR)
        val last = entry.lastIndexOf(SEPARATOR)
        require(first >= 0 && last >= first + SEPARATOR.length) {
            "Coverage exception '$entry' in $file must read 'path | source line | reason'!"
        }
        val path = entry.substring(0, first)
        val sourceLine = entry.substring(first + SEPARATOR.length, last)
        val reason = entry.substring(last + SEPARATOR.length)
        return CoverageException(path, sourceLine, reason)
    }
}

object CoverageReport {

    private const val LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd"

    private const val JAVA_EXTENSION: String = "java"
    private const val PATH_SEPARATOR: String = "/"

    private const val SOURCE_FILE_TAG: String = "sourcefile"
    private const val LINE_TAG: String = "line"

    private const val NAME_ATTRIBUTE: String = "name"
    private const val LINE_NUMBER_ATTRIBUTE: String = "nr"
    private const val MISSED_INSTRUCTIONS_ATTRIBUTE: String = "mi"
    private const val COVERED_INSTRUCTIONS_ATTRIBUTE: String = "ci"
    private const val MISSED_BRANCHES_ATTRIBUTE: String = "mb"
    private const val COVERED_BRANCHES_ATTRIBUTE: String = "cb"

    private const val FIRST_LINE_NUMBER: Int = 1

    fun findGaps(report: File, sourceDirectory: File, exceptions: List<CoverageException>): List<String> {
        if (!report.isFile) {
            val hasSources = sourceDirectory.isDirectory && sourceDirectory.walk().any { it.extension == JAVA_EXTENSION }
            return if (hasSources) listOf("$sourceDirectory: warning: no test ran, so none of this code is covered.") else emptyList()
        }
        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature(LOAD_EXTERNAL_DTD, false)
        val document = factory.newDocumentBuilder().parse(report)
        val sourceFiles = document.getElementsByTagName(SOURCE_FILE_TAG)
        val gaps = mutableListOf<String>()
        for (index in 0 until sourceFiles.length) {
            val sourceFile = sourceFiles.item(index) as Element
            gaps += findGapsInFile(sourceFile, sourceDirectory, exceptions)
        }
        return gaps
    }

    private fun findGapsInFile(sourceFile: Element, sourceDirectory: File, exceptions: List<CoverageException>): List<String> {
        val packageElement = sourceFile.parentNode as Element
        val relativePath = packageElement.getAttribute(NAME_ATTRIBUTE) + PATH_SEPARATOR + sourceFile.getAttribute(NAME_ATTRIBUTE)
        val file = File(sourceDirectory, relativePath)
        val sourceLines by lazy { if (file.isFile) file.readLines() else emptyList() }
        val lines = sourceFile.getElementsByTagName(LINE_TAG)
        val gaps = mutableListOf<String>()
        for (index in 0 until lines.length) {
            val line = lines.item(index) as Element
            val gap = describeGap(line) ?: continue
            val number = line.getAttribute(LINE_NUMBER_ATTRIBUTE).toInt()
            val text = sourceLines.getOrNull(number - FIRST_LINE_NUMBER)?.trim() ?: ""
            val exception = exceptions.firstOrNull { !it.used && it.path == relativePath && it.sourceLine == text }
            if (exception == null) {
                gaps += "$file:$number: warning: $gap."
            } else {
                exception.used = true
            }
        }
        return gaps
    }

    private fun describeGap(line: Element): String? {
        val missedInstructions = line.getAttribute(MISSED_INSTRUCTIONS_ATTRIBUTE).toInt()
        val coveredInstructions = line.getAttribute(COVERED_INSTRUCTIONS_ATTRIBUTE).toInt()
        val missedBranches = line.getAttribute(MISSED_BRANCHES_ATTRIBUTE).toInt()
        val coveredBranches = line.getAttribute(COVERED_BRANCHES_ATTRIBUTE).toInt()
        return when {
            coveredInstructions == 0 && missedInstructions > 0 -> "line is not covered by any test"
            missedBranches > 0 -> "$missedBranches of ${missedBranches + coveredBranches} branches are not covered by any test"
            missedInstructions > 0 ->
                "$missedInstructions of ${missedInstructions + coveredInstructions} instructions on this line are not covered by any test"
            else -> null
        }
    }
}
