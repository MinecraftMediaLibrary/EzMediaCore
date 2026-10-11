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

object JavaSource {

    private const val TEXT_BLOCK_QUOTES = "\"\"\""

    fun mask(source: String): String = Masker(source, true).mask()

    fun withoutComments(source: String): String = Masker(source, false).mask()

    private enum class State { CODE, LINE_COMMENT, BLOCK_COMMENT, TEXT_BLOCK, STRING, CHARACTER }

    private class Masker(private val source: String, private val maskLiterals: Boolean) {

        private val out = StringBuilder(source.length)
        private var state = State.CODE
        private var index = 0

        fun mask(): String {
            while (index < source.length) {
                when (state) {
                    State.CODE -> code()
                    State.LINE_COMMENT -> lineComment()
                    State.BLOCK_COMMENT -> blockComment()
                    State.TEXT_BLOCK -> textBlock()
                    State.STRING -> literal('"')
                    State.CHARACTER -> literal('\'')
                }
            }
            return out.toString()
        }

        private fun code() {
            when {
                source.startsWith("//", index) -> enter(State.LINE_COMMENT, 2)
                source.startsWith("/*", index) -> enter(State.BLOCK_COMMENT, 2)
                source.startsWith(TEXT_BLOCK_QUOTES, index) -> enter(State.TEXT_BLOCK, TEXT_BLOCK_QUOTES.length)
                source[index] == '"' -> enter(State.STRING, 1)
                source[index] == '\'' -> enter(State.CHARACTER, 1)
                else -> keep()
            }
        }

        private fun lineComment() {
            if (source[index] == '\n') {
                state = State.CODE
            }
            blank(1)
        }

        private fun blockComment() {
            if (source.startsWith("*/", index)) {
                enter(State.CODE, 2)
            } else {
                blank(1)
            }
        }

        private fun textBlock() {
            when {
                source[index] == '\\' -> literalCharacters(2)
                source.startsWith(TEXT_BLOCK_QUOTES, index) -> enter(State.CODE, TEXT_BLOCK_QUOTES.length)
                else -> literalCharacters(1)
            }
        }

        private fun literal(quote: Char) {
            when (source[index]) {
                '\\' -> literalCharacters(2)
                quote, '\n' -> enter(State.CODE, 1)
                else -> literalCharacters(1)
            }
        }

        private fun enter(next: State, length: Int) {
            val comment = state == State.LINE_COMMENT || state == State.BLOCK_COMMENT ||
                next == State.LINE_COMMENT || next == State.BLOCK_COMMENT
            state = next
            if (comment) blank(length) else literalCharacters(length)
        }

        private fun literalCharacters(length: Int) {
            if (maskLiterals) {
                blank(length)
            } else {
                repeat(minOf(length, source.length - index)) { keep() }
            }
        }

        private fun blank(length: Int) {
            val end = minOf(index + length, source.length)
            while (index < end) {
                out.append(if (source[index] == '\n') '\n' else ' ')
                index++
            }
        }

        private fun keep() {
            out.append(source[index])
            index++
        }
    }
}
