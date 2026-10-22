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

/**
 * One entry of a coverage exceptions file: a source line that no test can cover, and why.
 *
 * @property path       the path of the source file, relative to the source directory
 * @property sourceLine the trimmed text of the excused line
 * @property reason     why no test can cover the line
 */
class CoverageException(val path: String, val sourceLine: String, val reason: String) {

    /**
     * Whether this exception has excused an uncovered line.
     */
    var used: Boolean = false
}
