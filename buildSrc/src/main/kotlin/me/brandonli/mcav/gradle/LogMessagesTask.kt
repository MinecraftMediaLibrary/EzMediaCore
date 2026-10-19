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

        const val CONCATENATED_MESSAGE: String =
            "{}:{}: error: the message of {} joins strings with +; use SLF4J placeholders for the arguments instead."
    }

    override fun findViolations(files: List<File>, reportViolation: (File, Number, List<String>) -> Unit): Unit {
        for (file in files) {
            val source: String = file.readText()
            val messages: List<ConcatenatedLogMessage> = LogMessages.find(source)
            messages.forEach { found -> reportViolation(file, found.line, listOf(found.call)) }
        }
    }

    override fun diagnosticMessage(): String {
        return CONCATENATED_MESSAGE
    }

    override fun failureMessage(count: Int): String {
        return "Found $count log calls with a concatenated message!"
    }

    override fun successMessage(fileCount: Int): String {
        return "no log call with a concatenated message in $fileCount files\n"
    }
}
