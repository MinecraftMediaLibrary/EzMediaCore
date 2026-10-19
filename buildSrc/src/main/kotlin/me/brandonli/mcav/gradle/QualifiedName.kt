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

import java.util.Objects

/**
 * A fully qualified type name written out in Java code.
 *
 * @property line the line of the name in its source file
 * @property name the name, such as `java.util.List`
 */
class QualifiedName(val line: Int, val name: String) {

    /**
     * Returns whether [other] is a name at the same line with the same text.
     *
     * @param other the object to compare with
     * @return whether both lines and both names match
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is QualifiedName) {
            return false
        }

        return this.line == other.line && this.name == other.name
    }

    /**
     * Returns a hash code built from the line and the name.
     *
     * @return the hash code
     */
    override fun hashCode(): Int {
        return Objects.hash(this.line, this.name)
    }

    /**
     * Returns a debugging representation with the line and the name.
     *
     * @return the representation
     */
    override fun toString(): String {
        return "QualifiedName(line=${this.line}, name=${this.name})"
    }
}
