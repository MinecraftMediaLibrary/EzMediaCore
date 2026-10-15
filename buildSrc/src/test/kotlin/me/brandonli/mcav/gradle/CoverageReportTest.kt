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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class CoverageReportTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun partialInstructionsAndBranchesRemainGaps() {
        val source = directory.resolve("sample/Example.java").toFile()
        source.parentFile.mkdirs()
        source.writeText("uncalled();\nchoose();\nRunnable action = () -> work();\ncovered();\n")
        val report = directory.resolve("report.xml").toFile()
        report.writeText("""
            <report name="fixture"><package name="sample"><sourcefile name="Example.java">
              <line nr="1" mi="3" ci="0" mb="0" cb="0"/>
              <line nr="2" mi="0" ci="3" mb="1" cb="1"/>
              <line nr="3" mi="2" ci="4" mb="0" cb="0"/>
              <line nr="4" mi="0" ci="3" mb="0" cb="0"/>
            </sourcefile></package></report>
        """.trimIndent())

        val gaps = CoverageReport.findGaps(report, directory.toFile(), emptyList())

        assertEquals(listOf(
            "$source:1: warning: line is not covered by any test",
            "$source:2: warning: 1 of 2 branches are not covered by any test",
            "$source:3: warning: 2 of 6 instructions on this line are not covered by any test"
        ), gaps)
    }

    @Test
    fun exceptionExcusesExactlyOneMatchingUncoveredLine() {
        val source = directory.resolve("sample/Example.java").toFile()
        source.parentFile.mkdirs()
        source.writeText("serverOnly();\nserverOnly();\ncovered();\n")
        val report = directory.resolve("report.xml").toFile()
        report.writeText("""
            <report name="fixture"><package name="sample"><sourcefile name="Example.java">
              <line nr="1" mi="1" ci="0" mb="0" cb="0"/>
              <line nr="2" mi="1" ci="0" mb="0" cb="0"/>
              <line nr="3" mi="0" ci="1" mb="0" cb="0"/>
            </sourcefile></package></report>
        """.trimIndent())
        val matching = CoverageException("sample/Example.java", "serverOnly();", "server constructor")
        val stale = CoverageException("sample/Example.java", "covered();", "obsolete exception")
        val wrongPath = CoverageException("other/Example.java", "serverOnly();", "different source")

        val gaps = CoverageReport.findGaps(report, directory.toFile(), listOf(matching, stale, wrongPath))

        assertEquals(listOf("$source:2: warning: line is not covered by any test"), gaps)
        assertTrue(matching.used)
        assertFalse(stale.used)
        assertFalse(wrongPath.used)
    }

    @Test
    fun missingReportFailsOnlyWhenProductionSourcesExist() {
        val report = directory.resolve("missing.xml").toFile()
        val sources = directory.resolve("sources").toFile()
        assertTrue(CoverageReport.findGaps(report, sources, emptyList()).isEmpty())
        sources.mkdirs()
        assertTrue(CoverageReport.findGaps(report, sources, emptyList()).isEmpty())
        sources.resolve("Example.java").writeText("class Example {}\n")
        assertEquals(listOf("$sources: warning: no test ran, so none of this code is covered"),
            CoverageReport.findGaps(report, sources, emptyList()))
    }

    @Test
    fun exceptionsRequireAReasonAndPreserveSourceSeparators() {
        val file = directory.resolve("exceptions.txt").toFile()
        assertTrue(CoverageExceptions.read(file).isEmpty())
        file.writeText("# comment\n\nsample/Example.java | left | right | native boundary\n")
        val entries = CoverageExceptions.read(file)
        assertEquals(1, entries.size)
        assertEquals("sample/Example.java", entries[0].path)
        assertEquals("left | right", entries[0].sourceLine)
        assertEquals("native boundary", entries[0].reason)
        for (invalid in listOf("missing separators", "path | source", "path | source | ")) {
            file.writeText(invalid)
            assertThrows(IllegalArgumentException::class.java) { CoverageExceptions.read(file) }
        }
    }

    @Test
    fun shouldReadAMissingSourceAsBlankLinesAndSkipALineWithoutInstructions(): Unit {
        val report = directory.resolve("report.xml").toFile()
        report.writeText("""
            <report name="fixture"><package name="sample"><sourcefile name="Missing.java">
              <line nr="1" mi="3" ci="0" mb="0" cb="0"/>
              <line nr="2" mi="0" ci="0" mb="0" cb="0"/>
            </sourcefile></package></report>
        """.trimIndent())
        val blankLine = CoverageException("sample/Missing.java", "", "generated source")

        val gaps: List<String> = CoverageReport.findGaps(report, directory.toFile(), listOf(blankLine))
        assertEquals(emptyList<String>(), gaps)
        assertTrue(blankLine.used)
    }
}
