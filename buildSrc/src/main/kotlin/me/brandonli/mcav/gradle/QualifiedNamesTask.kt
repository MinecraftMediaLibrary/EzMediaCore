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

abstract class QualifiedNamesTask : JavaLintTask() {

    private companion object {
        const val WRITTEN_OUT = "{}:{}: error: {} is written out; import it, or mark the line // fqn: <why> if its simple name clashes."
    }

    override fun findViolations(files: List<File>, reportViolation: (File, Number, List<String>) -> Unit) {
        files.forEach { file ->
            QualifiedNames.find(file.readText()).forEach { found ->
                reportViolation(file, found.line, listOf(found.name))
            }
        }
    }

    override fun diagnosticMessage(): String = WRITTEN_OUT

    override fun failureMessage(count: Int): String = "Found $count fully qualified type names in Java code!"

    override fun successMessage(fileCount: Int): String = "no fully qualified type names in $fileCount files\n"
}
