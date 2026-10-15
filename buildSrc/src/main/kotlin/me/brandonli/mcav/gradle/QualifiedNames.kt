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

    private val ROOTS: List<String> = listOf(
        "java", "javax", "jakarta", "jdk", "sun", "com", "org", "net", "io", "me", "uk", "de", "xyz", "it", "dev", "info"
    )

    private val NAME: Regex = Regex("(?<![\\w.$])((?:${ROOTS.joinToString(ROOT_SEPARATOR)})\\.(?:[a-z_][a-z0-9_]*\\.)+[A-Z][\\w$]*)")

    private val MARKER: Regex = Regex("//\\s*fqn:\\s*\\S")

    private val DECLARATION: Regex = Regex("^\\s*(package|import)\\s")

    private const val ROOT_SEPARATOR: String = "|"
    private const val NEW_LINE: Char = '\n'
    private const val NAME_GROUP: Int = 1
    private const val FIRST_LINE_NUMBER: Int = 1

    fun find(source: String): List<QualifiedName> {
        val lines: List<String> = source.split(NEW_LINE)
        val code: List<String> = JavaSource.mask(source).split(NEW_LINE)
        val found: MutableList<QualifiedName> = mutableListOf<QualifiedName>()
        code.forEachIndexed { index, line ->
            if (DECLARATION.containsMatchIn(line) || MARKER.containsMatchIn(lines[index])) {
                return@forEachIndexed
            }
            NAME.findAll(line).forEach { match -> found += QualifiedName(index + FIRST_LINE_NUMBER, match.groupValues[NAME_GROUP]) }
        }
        return found
    }
}
