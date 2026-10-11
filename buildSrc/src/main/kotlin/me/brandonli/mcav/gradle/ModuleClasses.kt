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
import java.util.SortedSet
import java.util.zip.ZipFile

object ModuleClasses {

    private const val CLASS_SUFFIX = ".class"

    private val NAME = Regex("me/brandonli/mcav/[A-Za-z0-9_$/]+")

    fun referenced(classFile: ByteArray): Set<String> =
        NAME.findAll(String(classFile, Charsets.ISO_8859_1)).map { it.value }.toSet()

    fun available(roots: Iterable<File>): Set<String> {
        val names = mutableSetOf<String>()
        roots.filter { it.exists() }.forEach { root ->
            if (root.isDirectory) {
                root.walkTopDown().filter { it.isFile && it.name.endsWith(CLASS_SUFFIX) }.forEach { file ->
                    names.add(file.relativeTo(root).invariantSeparatorsPath.removeSuffix(CLASS_SUFFIX))
                }
            } else {
                ZipFile(root).use { jar ->
                    jar.entries().asSequence().map { it.name }.filter { it.endsWith(CLASS_SUFFIX) }.forEach {
                        names.add(it.removeSuffix(CLASS_SUFFIX))
                    }
                }
            }
        }
        return names.filter { NAME.matches(it) }.toSet()
    }

    fun required(pluginClasses: Iterable<File>, modules: Iterable<File>): SortedSet<String> {
        val inModules = available(modules)
        val used = sortedSetOf<String>()
        pluginClasses.filter { it.isDirectory }.forEach { root ->
            root.walkTopDown().filter { it.isFile && it.name.endsWith(CLASS_SUFFIX) }.forEach { file ->
                used.addAll(referenced(file.readBytes()).filter { it in inModules })
            }
        }
        return used
    }
}
