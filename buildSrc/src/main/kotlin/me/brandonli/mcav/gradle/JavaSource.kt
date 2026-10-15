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

        private val out: StringBuilder = StringBuilder(this.source.length)
        private var state: State = State.CODE
        private var index: Int = 0

        fun mask(): String {
            while (this.index < this.source.length) {
                when (this.state) {
                    State.CODE -> this.code()
                    State.LINE_COMMENT -> this.lineComment()
                    State.BLOCK_COMMENT -> this.blockComment()
                    State.TEXT_BLOCK -> this.textBlock()
                    State.STRING -> this.literal(STRING_QUOTE)
                    State.CHARACTER -> this.literal(CHARACTER_QUOTE)
                }
            }
            return this.out.toString()
        }

        private fun code(): Unit {
            when {
                this.source.startsWith(LINE_COMMENT_START, this.index) -> this.enter(State.LINE_COMMENT, LINE_COMMENT_START.length)
                this.source.startsWith(BLOCK_COMMENT_START, this.index) -> this.enter(State.BLOCK_COMMENT, BLOCK_COMMENT_START.length)
                this.source.startsWith(TEXT_BLOCK_QUOTES, this.index) -> this.enter(State.TEXT_BLOCK, TEXT_BLOCK_QUOTES.length)
                this.source[this.index] == STRING_QUOTE -> this.enter(State.STRING, 1)
                this.source[this.index] == CHARACTER_QUOTE -> this.enter(State.CHARACTER, 1)
                else -> this.keep()
            }
        }

        private fun lineComment(): Unit {
            if (this.source[this.index] == NEW_LINE) {
                this.state = State.CODE
            }
            this.blank(1)
        }

        private fun blockComment(): Unit {
            if (this.source.startsWith(BLOCK_COMMENT_END, this.index)) {
                this.enter(State.CODE, BLOCK_COMMENT_END.length)
            } else {
                this.blank(1)
            }
        }

        private fun textBlock(): Unit {
            when {
                this.source[this.index] == ESCAPE -> this.literalCharacters(ESCAPE_SEQUENCE_LENGTH)
                this.source.startsWith(TEXT_BLOCK_QUOTES, this.index) -> this.enter(State.CODE, TEXT_BLOCK_QUOTES.length)
                else -> this.literalCharacters(1)
            }
        }

        private fun literal(quote: Char): Unit {
            when (this.source[this.index]) {
                ESCAPE -> this.literalCharacters(ESCAPE_SEQUENCE_LENGTH)
                quote, NEW_LINE -> this.enter(State.CODE, 1)
                else -> this.literalCharacters(1)
            }
        }

        private fun enter(next: State, length: Int): Unit {
            val comment: Boolean = this.state == State.BLOCK_COMMENT ||
                next == State.LINE_COMMENT || next == State.BLOCK_COMMENT
            this.state = next
            if (comment) this.blank(length) else this.literalCharacters(length)
        }

        private fun literalCharacters(length: Int): Unit {
            if (this.maskLiterals) {
                this.blank(length)
            } else {
                repeat(minOf(length, this.source.length - this.index)) { this.keep() }
            }
        }

        private fun blank(length: Int): Unit {
            val end: Int = minOf(this.index + length, this.source.length)
            while (this.index < end) {
                this.out.append(if (this.source[this.index] == NEW_LINE) NEW_LINE else BLANK)
                this.index++
            }
        }

        private fun keep(): Unit {
            this.out.append(this.source[this.index])
            this.index++
        }
    }
}
