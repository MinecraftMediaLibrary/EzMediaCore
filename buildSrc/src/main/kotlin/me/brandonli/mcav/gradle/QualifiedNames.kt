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

data class QualifiedName(val line: Int, val name: String)

object QualifiedNames {

    private val ROOTS = listOf(
        "java", "javax", "jakarta", "jdk", "sun", "com", "org", "net", "io", "me", "uk", "de", "xyz", "it", "dev", "info"
    )

    private val NAME = Regex("(?<![\\w.$])((?:${ROOTS.joinToString("|")})\\.(?:[a-z_][a-z0-9_]*\\.)+[A-Z][\\w$]*)")

    private val MARKER = Regex("//\\s*fqn:\\s*\\S")

    private val DECLARATION = Regex("^\\s*(package|import)\\s")

    fun find(source: String): List<QualifiedName> {
        val lines = source.split('\n')
        val code = JavaSource.mask(source).split('\n')
        val found = mutableListOf<QualifiedName>()
        code.forEachIndexed { index, line ->
            if (DECLARATION.containsMatchIn(line) || MARKER.containsMatchIn(lines[index])) {
                return@forEachIndexed
            }
            NAME.findAll(line).forEach { match -> found += QualifiedName(index + 1, match.groupValues[1]) }
        }
        return found
    }
}
