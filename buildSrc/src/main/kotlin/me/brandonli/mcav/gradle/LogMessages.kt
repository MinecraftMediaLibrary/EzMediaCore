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

data class ConcatenatedLogMessage(val line: Int, val call: String)

object LogMessages {

    private val CALL: Regex = Regex("""\b([A-Za-z_][A-Za-z0-9_]*)\s*\.\s*(trace|debug|info|warn|error)\s*\(""")

    private val LOGGER: Regex = Regex("""(?:[A-Z][A-Z0-9_]*_)?LOG(?:GER)?|log(?:ger)?|[a-z][A-Za-z0-9]*Logger""")

    private const val RECEIVER_GROUP: Int = 1

    private const val CALL_START: Char = '('
    private const val ARGUMENT_SEPARATOR: Char = ','
    private const val CONCATENATION: Char = '+'
    private const val STRING_QUOTE: Char = '"'
    private const val NEW_LINE: Char = '\n'

    private const val FIRST_LINE_NUMBER: Int = 1

    private val OPENING: Set<Char> = setOf('(', '[', '{')

    private val CLOSING: Set<Char> = setOf(')', ']', '}')

    fun find(source: String): List<ConcatenatedLogMessage> {
        val code: String = JavaSource.mask(source)
        val text: String = JavaSource.withoutComments(source)
        return CALL.findAll(code)
            .filter { LOGGER.matches(it.groupValues[RECEIVER_GROUP]) }
            .filter { call -> this.isConcatenated(this.arguments(text, code, call.range.last + 1)) }
            .map { call -> ConcatenatedLogMessage(this.lineOf(source, call.range.first), call.value.substringBefore(CALL_START)) }
            .toList()
    }

    private fun isConcatenated(arguments: List<Argument>): Boolean {
        return arguments.any { it.joinsStrings() }
    }

    private fun arguments(source: String, code: String, start: Int): List<Argument> {
        val arguments: MutableList<LogMessages.Argument> = mutableListOf<Argument>()
        var depth: Int = 0
        var argumentStart: Int = start
        for (index in start until code.length) {
            val character: Char = code[index]
            val ends: Boolean = character in CLOSING && depth == 0
            when {
                character in OPENING -> depth++
                character in CLOSING && depth > 0 -> depth--
                ends || (character == ARGUMENT_SEPARATOR && depth == 0) -> {
                    arguments += Argument(source.substring(argumentStart, index), code.substring(argumentStart, index))
                    argumentStart = index + 1
                }
            }
            if (ends) {
                break
            }
        }
        val noArguments: Boolean = arguments.size == 1 && arguments[0].text.isBlank()
        return if (noArguments) emptyList() else arguments
    }

    private fun lineOf(source: String, offset: Int): Int {
        return source.substring(0, offset).count { it == NEW_LINE } + FIRST_LINE_NUMBER
    }

    private class Argument(val text: String, private val code: String) {

        fun joinsStrings(): Boolean {
            var depth: Int = 0
            var plus: Boolean = false
            var literal: Boolean = false
            this.code.forEachIndexed { index, character ->
                when {
                    character in OPENING -> depth++
                    character in CLOSING -> depth--
                    depth > 0 -> Unit
                    character == CONCATENATION -> plus = true
                    this.text[index] == STRING_QUOTE -> literal = true
                }
            }
            return plus && literal
        }
    }
}
