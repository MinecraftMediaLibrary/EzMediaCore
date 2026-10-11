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

    @TempDir
    lateinit var directory: Path

    @Test
    fun splitsHalfOfTheBudgetOverTheIterationsOfEveryRun() {
        assertEquals(76L, JcstressBudget.iterationMillis("quick", 10, 9, 4))
    }

    @Test
    fun keepsTheIterationTimeWithinItsBounds() {
        assertEquals(50L, JcstressBudget.iterationMillis("quick", 1, 9, 4))
        assertEquals(10_000L, JcstressBudget.iterationMillis("sanity", 100_000, 1, 4))
    }

    @Test
    fun runsOneRunAtATimeOnASingleCpu() {
        assertEquals(314L, JcstressBudget.iterationMillis("default", 10, 1, 1))
    }

    @Test
    fun refusesAnUnknownMode() {
        assertThrows(GradleException::class.java) { JcstressBudget.iterationMillis("forever", 10, 1, 4) }
    }

    @Test
    fun countsTheTestsTheSelectionMatches() {
        val jar = directory.resolve("tests.jar").toFile()
        ZipOutputStream(jar.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("META-INF/TestList"))
            zip.write(listOf("JCTEST9Sa.b.First,x", "JCTEST10Sa.b.Second,y", "other line").joinToString("\n").toByteArray())
            zip.closeEntry()
        }
        assertEquals(2, JcstressBudget.countTests(jar, null))
        assertEquals(1, JcstressBudget.countTests(jar, "Sec"))
        assertEquals(0, JcstressBudget.countTests(jar, "Third"))
    }

    @Test
    fun countsNoTestsInAJarWithoutTheList() {
        val jar = directory.resolve("empty.jar").toFile()
        ZipOutputStream(jar.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("other"))
            zip.closeEntry()
        }
        assertEquals(0, JcstressBudget.countTests(jar, null))
    }
}
