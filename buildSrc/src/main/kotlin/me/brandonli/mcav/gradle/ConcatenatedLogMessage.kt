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
 * A log call whose message is built by string concatenation instead of a template.
 *
 * @property line the line of the call in its source file
 * @property call the logger and the method that the call names, such as `LOGGER.info`
 */
class ConcatenatedLogMessage(val line: Int, val call: String) {

    /**
     * Returns whether [other] is a log call at the same line to the same logger method.
     *
     * @param other the object to compare with
     * @return whether both lines and both calls match
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ConcatenatedLogMessage) {
            return false
        }

        return this.line == other.line && this.call == other.call
    }

    /**
     * Returns a hash code built from the line and the call.
     *
     * @return the hash code
     */
    override fun hashCode(): Int {
        return Objects.hash(this.line, this.call)
    }

    /**
     * Returns a debugging representation with the line and the call.
     *
     * @return the representation
     */
    override fun toString(): String {
        return "ConcatenatedLogMessage(line=${this.line}, call=${this.call})"
    }
}
