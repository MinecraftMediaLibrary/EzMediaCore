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

class UndescriptiveNameTest {

    private companion object {

        const val LINE: Long = 12L
        const val OTHER_LINE: Long = 13L

        const val NAME: String = "sb"
        const val OTHER_NAME: String = "tmp"

        const val KIND: String = "local variable"
        const val OTHER_KIND: String = "field"

        const val MEMBER: String = "Sample#render"
        const val OTHER_MEMBER: String = "Sample#parse"
    }

    @Test
    fun shouldEqualOnlyANameOfTheSameLineTextKindAndMember(): Unit {
        val name: UndescriptiveName = UndescriptiveName(LINE, NAME, KIND, MEMBER)
        val same: UndescriptiveName = UndescriptiveName(LINE, NAME, KIND, MEMBER)
        val otherLine: UndescriptiveName = UndescriptiveName(OTHER_LINE, NAME, KIND, MEMBER)
        val otherName: UndescriptiveName = UndescriptiveName(LINE, OTHER_NAME, KIND, MEMBER)
        val otherKind: UndescriptiveName = UndescriptiveName(LINE, NAME, OTHER_KIND, MEMBER)
        val otherMember: UndescriptiveName = UndescriptiveName(LINE, NAME, KIND, OTHER_MEMBER)

        assertEquals(name, name)
        assertEquals(same, name)
        assertNotEquals(otherLine, name)
        assertNotEquals(otherName, name)
        assertNotEquals(otherKind, name)
        assertNotEquals(otherMember, name)
        assertNotEquals(name, NAME)

        val nameHash: Int = name.hashCode()
        val sameHash: Int = same.hashCode()
        assertEquals(sameHash, nameHash)

        val otherHash: Int = otherMember.hashCode()
        assertNotEquals(otherHash, nameHash)
    }

    @Test
    fun shouldShowTheLineTheTextTheKindAndTheMember(): Unit {
        val name: UndescriptiveName = UndescriptiveName(LINE, NAME, KIND, MEMBER)
        val text: String = name.toString()
        assertEquals("UndescriptiveName(line=12, name=sb, kind=local variable, member=Sample#render)", text)
    }
}
