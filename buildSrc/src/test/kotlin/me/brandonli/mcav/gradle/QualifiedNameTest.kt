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

class QualifiedNameTest {

    private companion object {

        const val LINE: Int = 3
        const val OTHER_LINE: Int = 4

        const val NAME: String = "java.util.List"
        const val OTHER_NAME: String = "java.util.Map"
    }

    @Test
    fun shouldEqualOnlyANameOfTheSameLineAndText(): Unit {
        val name: QualifiedName = QualifiedName(LINE, NAME)
        val same: QualifiedName = QualifiedName(LINE, NAME)
        val otherLine: QualifiedName = QualifiedName(OTHER_LINE, NAME)
        val otherName: QualifiedName = QualifiedName(LINE, OTHER_NAME)

        assertEquals(name, name)
        assertEquals(same, name)
        assertNotEquals(otherLine, name)
        assertNotEquals(otherName, name)
        assertNotEquals(name, NAME)

        val nameHash: Int = name.hashCode()
        val sameHash: Int = same.hashCode()
        assertEquals(sameHash, nameHash)

        val otherHash: Int = otherName.hashCode()
        assertNotEquals(otherHash, nameHash)
    }

    @Test
    fun shouldShowTheLineAndTheName(): Unit {
        val name: QualifiedName = QualifiedName(LINE, NAME)
        val text: String = name.toString()
        assertEquals("QualifiedName(line=3, name=java.util.List)", text)
    }
}
