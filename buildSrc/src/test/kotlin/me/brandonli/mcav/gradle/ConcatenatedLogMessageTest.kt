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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class ConcatenatedLogMessageTest {

    private companion object {

        const val LINE: Int = 7
        const val OTHER_LINE: Int = 8

        const val CALL: String = "LOGGER.info"
        const val OTHER_CALL: String = "LOGGER.warn"
    }

    @Test
    fun shouldEqualOnlyAMessageOfTheSameLineAndCall(): Unit {
        val message: ConcatenatedLogMessage = ConcatenatedLogMessage(LINE, CALL)
        val same: ConcatenatedLogMessage = ConcatenatedLogMessage(LINE, CALL)
        val otherLine: ConcatenatedLogMessage = ConcatenatedLogMessage(OTHER_LINE, CALL)
        val otherCall: ConcatenatedLogMessage = ConcatenatedLogMessage(LINE, OTHER_CALL)

        assertEquals(message, message)
        assertEquals(same, message)
        assertNotEquals(otherLine, message)
        assertNotEquals(otherCall, message)
        assertNotEquals(message, CALL)

        val messageHash: Int = message.hashCode()
        val sameHash: Int = same.hashCode()
        assertEquals(sameHash, messageHash)

        val otherHash: Int = otherCall.hashCode()
        assertNotEquals(otherHash, messageHash)
    }

    @Test
    fun shouldShowTheLineAndTheCall(): Unit {
        val message: ConcatenatedLogMessage = ConcatenatedLogMessage(LINE, CALL)
        val text: String = message.toString()
        assertEquals("ConcatenatedLogMessage(line=7, call=LOGGER.info)", text)
    }
}
