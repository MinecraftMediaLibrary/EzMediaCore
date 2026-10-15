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

import com.code_intelligence.jazzer.api.FuzzedDataProvider
import com.code_intelligence.jazzer.junit.FuzzTest
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag

// A tool prints its version as it likes, so any output must either pass or fail as a GradleException: it passes only
// when it holds the expected version after the prefix, and always when it starts with them.
@Tag("fuzz")
class ToolVersionsFuzzTest {

    private companion object {

        const val TOOL: String = "tool"
        const val PREFIX: String = "tool "

        const val MAXIMUM_VERSION_LENGTH: Int = 16
        const val MAXIMUM_OUTPUT_LENGTH: Int = 256

        const val LINE_BREAK: String = "\n"
    }

    @FuzzTest
    fun shouldPassOnlyAnOutputThatHoldsTheExpectedVersion(data: FuzzedDataProvider): Unit {
        val versionText: String = data.consumeString(MAXIMUM_VERSION_LENGTH)
        val deliveredVersion: String = this.deliver(versionText)
        val expected: String = deliveredVersion.filterNot { it.isWhitespace() }
        if (expected.isEmpty()) {
            return
        }

        val output: String = data.consumeString(MAXIMUM_OUTPUT_LENGTH)
        val delivered: String = this.deliver(output)
        val passed: Boolean = this.verifies(delivered, expected)
        val held: Boolean = !passed || delivered.contains(PREFIX + expected)
        assertTrue(held, "An output that passes must hold the expected version!")

        val leading: String = PREFIX + expected + LINE_BREAK + delivered
        val leadingPassed: Boolean = this.verifies(leading, expected)
        assertTrue(leadingPassed, "An output that starts with the expected version must pass!")
    }

    // a process hands over bytes, so text that UTF-8 cannot carry, such as a lone surrogate, arrives replaced
    private fun deliver(text: String): String {
        val bytes: ByteArray = text.toByteArray(Charsets.UTF_8)
        return String(bytes, Charsets.UTF_8)
    }

    private fun verifies(output: String, expected: String): Boolean {
        val processes: RecordedProcesses = RecordedProcesses(output)
        try {
            ToolVersions.verify(processes.operations, TOOL, TOOL, expected, PREFIX)
        } catch (exception: GradleException) {
            return false
        }

        return true
    }
}
