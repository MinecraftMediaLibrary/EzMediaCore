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

abstract class LogMessagesTask : JavaLintTask() {

    private companion object {
        const val INLINE_MESSAGE =
            "{}:{}: error: the message of {} is written inline; name it in a private static final String constant, " +
                "with SLF4J placeholders for the arguments and no string concatenation"
    }

    override fun findViolations(files: List<File>, reportViolation: (File, Number, List<String>) -> Unit) {
        files.forEach { file ->
            LogMessages.find(file.readText()).forEach { found ->
                reportViolation(file, found.line, listOf(found.call))
            }
        }
    }

    override fun diagnosticMessage(): String = INLINE_MESSAGE

    override fun failureMessage(count: Int): String = "$count log calls with an inline message"

    override fun successMessage(fileCount: Int): String = "no log call with an inline message in $fileCount files\n"
}
