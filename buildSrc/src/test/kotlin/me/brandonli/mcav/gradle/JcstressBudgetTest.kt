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

import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class JcstressBudgetTest {

    private companion object {

        const val QUICK_PRESET: String = "quick"

        const val TESTS_JAR: String = "tests.jar"
        const val EMPTY_JAR: String = "empty.jar"
        const val TEST_LIST_ENTRY: String = "META-INF/TestList"

        const val OTHER_ENTRY: String = "other"
    }

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldSplitHalfOfTheBudgetOverTheIterationsOfEveryRun() {
        val iterationMilliseconds: Long = JcstressBudget.iterationMilliseconds(QUICK_PRESET, 10, 9, 4)
        assertEquals(76L, iterationMilliseconds)
    }

    @Test
    fun shouldKeepTheIterationTimeWithinItsBounds() {
        val shortest: Long = JcstressBudget.iterationMilliseconds(QUICK_PRESET, 1, 9, 4)
        assertEquals(50L, shortest)

        val longest: Long = JcstressBudget.iterationMilliseconds("sanity", 100_000, 1, 4)
        assertEquals(10_000L, longest)
    }

    @Test
    fun shouldRunOneRunAtATimeOnASingleCpu() {
        val iterationMilliseconds: Long = JcstressBudget.iterationMilliseconds("default", 10, 1, 1)
        assertEquals(314L, iterationMilliseconds)
    }

    @Test
    fun shouldRefuseAnUnknownMode() {
        assertThrows(GradleException::class.java) { JcstressBudget.iterationMilliseconds("forever", 10, 1, 4) }
    }

    @Test
    fun shouldCountTheTestsThatTheSelectionMatches() {
        val jar = directory.resolve(TESTS_JAR).toFile()
        ZipOutputStream(jar.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry(TEST_LIST_ENTRY))
            zip.write(listOf("JCTEST9Sa.b.First,x", "JCTEST10Sa.b.Second,y", "other line").joinToString("\n").toByteArray())
            zip.closeEntry()
        }
        val all: Int = JcstressBudget.countTests(jar, null)
        assertEquals(2, all)

        val selected: Int = JcstressBudget.countTests(jar, "Sec")
        assertEquals(1, selected)

        val unmatched: Int = JcstressBudget.countTests(jar, "Third")
        assertEquals(0, unmatched)
    }

    @Test
    fun shouldCountNoTestsInAJarWithoutTheList() {
        val jar = directory.resolve(EMPTY_JAR).toFile()
        ZipOutputStream(jar.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry(OTHER_ENTRY))
            zip.closeEntry()
        }
        val count: Int = JcstressBudget.countTests(jar, null)
        assertEquals(0, count)
    }
}
