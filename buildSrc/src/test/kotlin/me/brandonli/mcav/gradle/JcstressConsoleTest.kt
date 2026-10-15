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

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class JcstressConsoleTest {

    @TempDir
    lateinit var directory: Path

    private val summary = listOf(
        "RUN RESULTS:",
        "  Interesting tests: No matches.",
        "  Failed tests: No matches.",
        "  Error tests: No matches.",
        "  All remaining tests: 2 matching test results.",
        "..... [OK] me.brandonli.mcav.http.ListenerStopRace",
        "..... [SKIPPED] me.brandonli.mcav.bukkit.media.mcv2.KeyframeRequestRace",
    )

    @Test
    fun shouldFindTheTestsThatARunSkipped() {
        val skipped: List<String> = JcstressConsole.skippedTests(this.summary)
        assertEquals(listOf("me.brandonli.mcav.bukkit.media.mcv2.KeyframeRequestRace"), skipped)
    }

    @Test
    fun shouldNameEverySkippedTestOnce() {
        val twice = this.summary + "..... [SKIPPED] me.brandonli.mcav.bukkit.media.mcv2.KeyframeRequestRace"
        val skippedCount: Int = JcstressConsole.skippedTests(twice).size
        assertEquals(1, skippedCount)
    }

    @Test
    fun shouldFailARunThatSkippedATest() {
        val console = this.console(this.summary)
        val failure = assertThrows(GradleException::class.java) { JcstressConsole.checkNoneSkipped(console) }
        val message: String? = failure.message
        assertEquals(
            "jcstress skipped 1 test(s), which failed their sanity check and never ran: " +
                "me.brandonli.mcav.bukkit.media.mcv2.KeyframeRequestRace",
            message,
        )
    }

    @Test
    fun shouldPassARunThatRanEveryTest() {
        JcstressConsole.checkNoneSkipped(this.console(this.summary.dropLast(1)))
    }

    @Test
    fun shouldKeepWhatItShowsAndLeaveTheConsoleOpen() {
        var closed = false
        val shown = object : ByteArrayOutputStream() {
            override fun close() {
                closed = true
            }
        }
        val file = this.directory.resolve("console.txt").toFile()
        val tee = JcstressConsole.tee(shown, file)
        tee.write("[OK] one\n".toByteArray())
        tee.write('x'.code)
        tee.close()
        val consoleClosed: Boolean = closed
        assertFalse(consoleClosed, "the build's console stays open for everything after the run")

        shown.write('!'.code)
        val shownText: String = shown.toString()
        assertEquals("[OK] one\nx!", shownText)

        val fileText: String = file.readText()
        assertEquals("[OK] one\nx", fileText)
    }

    private fun console(lines: List<String>): File {
        val file = this.directory.resolve("console.txt").toFile()
        file.writeText(lines.joinToString("\n"))
        return file
    }

    @Test
    fun shouldFlushTheConsoleAndTheFile(): Unit {
        var flushed = false
        val shown = object : ByteArrayOutputStream() {
            override fun flush(): Unit {
                flushed = true
            }
        }
        val file = this.directory.resolve("console.txt").toFile()
        val tee = JcstressConsole.tee(shown, file)
        tee.write("[OK] one".toByteArray())
        tee.flush()

        val written: String = file.readText()
        tee.close()
        val consoleFlushed: Boolean = flushed
        assertTrue(consoleFlushed)

        assertEquals("[OK] one", written)
    }
}
