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

/**
 * Finds the fuzz test methods of a module's Java test sources.
 *
 * Jazzer fuzzes only the first `@FuzzTest` method that runs in a JVM, so the build gives every method a test task of
 * its own. The methods are read from the sources when the build is configured, before any test class is compiled.
 */
object FuzzTargets {

    private const val FUZZ_TEST_SUFFIX: String = "FuzzTest.java"
    private const val JAVA_EXTENSION: String = ".java"

    private const val PATH_SEPARATOR: Char = '/'
    private const val PACKAGE_SEPARATOR: Char = '.'

    private const val ANNOTATION_ARGUMENTS: String = """(?:\s*\([^)]*\))?"""
    private const val OTHER_ANNOTATIONS: String = """(?:\s*@[\w.]+$ANNOTATION_ARGUMENTS)*"""
    private const val MODIFIERS: String = """(?:(?:public|protected|private|static|final)\s+)*"""

    private val FUZZ_TEST_METHOD: Regex = Regex("""@FuzzTest\b$ANNOTATION_ARGUMENTS$OTHER_ANNOTATIONS\s+${MODIFIERS}void\s+(\w+)\s*\(""")

    /**
     * Returns the fuzz test methods of every `*FuzzTest.java` file below the given source directories.
     *
     * @param sourceDirectories the Java test source directories
     * @return the methods, ordered by class and then by their place in the source
     */
    fun find(sourceDirectories: Iterable<File>): List<FuzzTarget> {
        val targets: List<FuzzTarget> = sourceDirectories.flatMap(this::findInDirectory)
        return targets.sortedBy { it.className }
    }

    /**
     * Returns the names of the fuzz test methods that a Java source declares, in their order in the source.
     *
     * Comments, strings and text blocks are ignored, so an annotation that is only mentioned in them declares nothing.
     *
     * @param source the Java source
     * @return the method names
     */
    fun findMethods(source: String): List<String> {
        val code: String = JavaSource.mask(source)
        return FUZZ_TEST_METHOD.findAll(code)
            .map { it.groupValues[1] }
            .toList()
    }

    private fun findInDirectory(directory: File): List<FuzzTarget> {
        val sources: List<File> = directory.walkTopDown()
            .filter { it.isFile && it.name.endsWith(FUZZ_TEST_SUFFIX) }
            .toList()
        return sources.flatMap { this.findInFile(directory, it) }
    }

    private fun findInFile(directory: File, source: File): List<FuzzTarget> {
        val relativePath: String = source.relativeTo(directory).invariantSeparatorsPath
        val className: String = relativePath.removeSuffix(JAVA_EXTENSION).replace(PATH_SEPARATOR, PACKAGE_SEPARATOR)
        val methods: List<String> = this.findMethods(source.readText())
        return methods.map { FuzzTarget(className, it) }
    }
}
