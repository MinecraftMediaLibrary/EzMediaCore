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

object RequirementsLock {

    const val PIN_SEPARATOR: String = "=="

    private const val PIN_END: Char = ' '

    fun missingPins(lock: String, versions: Map<String, String>): List<String> {
        val locked: Set<String> = lock.lines().map { line -> line.substringBefore(PIN_END) }.toSet()
        return versions.map { (name, version) -> "$name$PIN_SEPARATOR$version" }.filterNot { pin -> pin in locked }
    }
}
