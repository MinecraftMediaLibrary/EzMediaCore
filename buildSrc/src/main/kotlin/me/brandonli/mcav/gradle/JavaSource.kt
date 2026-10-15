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

    private const val TEXT_BLOCK_QUOTES: String = "\"\"\""

    private const val LINE_COMMENT_START: String = "//"
    private const val BLOCK_COMMENT_START: String = "/*"
    private const val BLOCK_COMMENT_END: String = "*/"

    private const val STRING_QUOTE: Char = '"'
    private const val CHARACTER_QUOTE: Char = '\''
    private const val ESCAPE: Char = '\\'
    private const val NEW_LINE: Char = '\n'
    private const val BLANK: Char = ' '

    private const val ESCAPE_SEQUENCE_LENGTH: Int = 2

    fun mask(source: String): String = Masker(source, true).mask()

    fun withoutComments(source: String): String = Masker(source, false).mask()

    private enum class State { CODE, LINE_COMMENT, BLOCK_COMMENT, TEXT_BLOCK, STRING, CHARACTER }

    private class Masker(private val source: String, private val maskLiterals: Boolean) {

        private val out: StringBuilder = StringBuilder(source.length)
        private var state: State = State.CODE
        private var index: Int = 0

        fun mask(): String {
            while (index < source.length) {
                when (state) {
                    State.CODE -> code()
                    State.LINE_COMMENT -> lineComment()
                    State.BLOCK_COMMENT -> blockComment()
                    State.TEXT_BLOCK -> textBlock()
                    State.STRING -> literal(STRING_QUOTE)
                    State.CHARACTER -> literal(CHARACTER_QUOTE)
                }
            }
            return out.toString()
        }

        private fun code(): Unit {
            when {
                source.startsWith(LINE_COMMENT_START, index) -> enter(State.LINE_COMMENT, LINE_COMMENT_START.length)
                source.startsWith(BLOCK_COMMENT_START, index) -> enter(State.BLOCK_COMMENT, BLOCK_COMMENT_START.length)
                source.startsWith(TEXT_BLOCK_QUOTES, index) -> enter(State.TEXT_BLOCK, TEXT_BLOCK_QUOTES.length)
                source[index] == STRING_QUOTE -> enter(State.STRING, 1)
                source[index] == CHARACTER_QUOTE -> enter(State.CHARACTER, 1)
                else -> keep()
            }
        }

        private fun lineComment(): Unit {
            if (source[index] == NEW_LINE) {
                state = State.CODE
            }
            blank(1)
        }

        private fun blockComment(): Unit {
            if (source.startsWith(BLOCK_COMMENT_END, index)) {
                enter(State.CODE, BLOCK_COMMENT_END.length)
            } else {
                blank(1)
            }
        }

        private fun textBlock(): Unit {
            when {
                source[index] == ESCAPE -> literalCharacters(ESCAPE_SEQUENCE_LENGTH)
                source.startsWith(TEXT_BLOCK_QUOTES, index) -> enter(State.CODE, TEXT_BLOCK_QUOTES.length)
                else -> literalCharacters(1)
            }
        }

        private fun literal(quote: Char): Unit {
            when (source[index]) {
                ESCAPE -> literalCharacters(ESCAPE_SEQUENCE_LENGTH)
                quote, NEW_LINE -> enter(State.CODE, 1)
                else -> literalCharacters(1)
            }
        }

        private fun enter(next: State, length: Int): Unit {
            val comment: Boolean = state == State.BLOCK_COMMENT ||
                next == State.LINE_COMMENT || next == State.BLOCK_COMMENT
            state = next
            if (comment) blank(length) else literalCharacters(length)
        }

        private fun literalCharacters(length: Int): Unit {
            if (maskLiterals) {
                blank(length)
            } else {
                repeat(minOf(length, source.length - index)) { keep() }
            }
        }

        private fun blank(length: Int): Unit {
            val end: Int = minOf(index + length, source.length)
            while (index < end) {
                out.append(if (source[index] == NEW_LINE) NEW_LINE else BLANK)
                index++
            }
        }

        private fun keep(): Unit {
            out.append(source[index])
            index++
        }
    }
}
