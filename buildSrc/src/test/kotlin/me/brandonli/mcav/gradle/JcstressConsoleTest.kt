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
import java.io.OutputStream
import java.nio.file.Path
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class JcstressConsoleTest {

    private companion object {

        const val SKIPPED_TEST: String = "me.brandonli.mcav.bukkit.media.mcv2.KeyframeRequestRace"
        const val SKIPPED_LINE: String = "..... [SKIPPED] me.brandonli.mcav.bukkit.media.mcv2.KeyframeRequestRace"

        const val CONSOLE_FILE: String = "console.txt"
    }

    @TempDir
    lateinit var directory: Path

    private val summary: List<String> = listOf(
        "RUN RESULTS:",
        "  Interesting tests: No matches.",
        "  Failed tests: No matches.",
        "  Error tests: No matches.",
        "  All remaining tests: 2 matching test results.",
        "..... [OK] me.brandonli.mcav.http.ListenerStopRace",
        SKIPPED_LINE,
    )

    @Test
    fun shouldFindTheTestsThatARunSkipped(): Unit {
        val skipped: List<String> = JcstressConsole.skippedTests(this.summary)
        assertEquals(listOf(SKIPPED_TEST), skipped)
    }

    @Test
    fun shouldNameEverySkippedTestOnce(): Unit {
        val twice: List<String> = this.summary + SKIPPED_LINE
        val skippedCount: Int = JcstressConsole.skippedTests(twice).size
        assertEquals(1, skippedCount)
    }

    @Test
    fun shouldFailARunThatSkippedATest(): Unit {
        val console: File = this.console(this.summary)
        val failure: GradleException = assertThrows(GradleException::class.java) { JcstressConsole.checkNoneSkipped(console) }
        val message: String? = failure.message
        assertEquals(
            "jcstress skipped 1 tests that failed their sanity check and never ran!\n" +
                SKIPPED_TEST,
            message,
        )
    }

    @Test
    fun shouldPassARunThatRanEveryTest(): Unit {
        JcstressConsole.checkNoneSkipped(this.console(this.summary.dropLast(1)))
    }

    @Test
    fun shouldKeepWhatItShowsAndLeaveTheConsoleOpen(): Unit {
        var closed: Boolean = false
        val shown: ByteArrayOutputStream = object : ByteArrayOutputStream() {
            override fun close(): Unit {
                closed = true
            }
        }
        val file: File = this.directory.resolve(CONSOLE_FILE).toFile()
        val tee: OutputStream = JcstressConsole.tee(shown, file)
        tee.write("[OK] one\n".toByteArray())
        tee.write('x'.code)
        tee.close()

        val consoleClosed: Boolean = closed
        assertFalse(consoleClosed, "The build's console must stay open for everything after the run!")

        shown.write('!'.code)

        val shownText: String = shown.toString()
        assertEquals("[OK] one\nx!", shownText)

        val fileText: String = file.readText()
        assertEquals("[OK] one\nx", fileText)
    }

    private fun console(lines: List<String>): File {
        val file: File = this.directory.resolve(CONSOLE_FILE).toFile()
        file.writeText(lines.joinToString("\n"))
        return file
    }

    @Test
    fun shouldFlushTheConsoleAndTheFileAndFlushTheConsoleAgainOnClose(): Unit {
        var flushes: Int = 0
        val shown: ByteArrayOutputStream = object : ByteArrayOutputStream() {
            override fun flush(): Unit {
                flushes++
            }
        }
        val file: File = this.directory.resolve(CONSOLE_FILE).toFile()
        val tee: OutputStream = JcstressConsole.tee(shown, file)
        tee.write("[OK] one".toByteArray())
        tee.flush()

        val written: String = file.readText()
        assertEquals("[OK] one", written)

        val flushesBeforeClose: Int = flushes
        assertEquals(1, flushesBeforeClose)

        tee.close()

        val flushesAfterClose: Int = flushes
        assertEquals(2, flushesAfterClose)
    }
}
