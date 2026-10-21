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

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assumptions.assumeTrue

object OpenFiles {

    private val DESCRIPTORS: Path = Path.of("/proc/self/fd")

    fun assumeListable(): Unit {
        val listable: Boolean = Files.isDirectory(DESCRIPTORS)
        assumeTrue(listable, "Open files can only be listed where /proc lists them!")
    }

    fun isOpen(file: File): Boolean {
        val target: Path = file.toPath().toRealPath()
        val descriptors: List<Path> = Files.list(DESCRIPTORS).use { it.toList() }
        return descriptors.any { this.pointsTo(it, target) }
    }

    private fun pointsTo(descriptor: Path, target: Path): Boolean {
        try {
            return Files.readSymbolicLink(descriptor) == target
        } catch (exception: IOException) {
            return false
        }
    }
}
