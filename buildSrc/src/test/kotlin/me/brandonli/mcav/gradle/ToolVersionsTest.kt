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
import org.gradle.api.GradleException
import org.gradle.process.ExecOperations
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ToolVersionsTest {

    private companion object {

        const val VERSION_OPTION: String = "--version"

        const val UV_TOOL: String = "uv"
        const val ZIG_TOOL: String = "zig"
    }

    @Test
    fun shouldAcceptTheExactUvVersionIncludingItsBuildMetadataAndRejectOtherVersions() {
        val processes = RecordedProcesses("uv 0.12.23 (build 2026-10-01)\n")
        ToolVersions.verify(processes.operations, File(UV_TOOL), UV_TOOL, "0.12.23")
        val commands: List<List<String>> = processes.commands
        assertEquals(listOf(listOf(UV_TOOL, VERSION_OPTION)), commands)

        val failure = assertThrows(GradleException::class.java) {
            ToolVersions.verify(RecordedProcesses("uv 0.12.22\n").operations, File(UV_TOOL), UV_TOOL, "0.12.23")
        }
        val message: String? = failure.message
        assertEquals("uv reports uv 0.12.22, which is not the uv 0.12.23 this build pins!", message)
    }

    @Test
    fun shouldAcceptTheExactClangFormatVersionWithAProviderPrefixAndRejectOtherToolsOrVersions() {
        val prefix = "clang-format version "
        val processes = RecordedProcesses("Ubuntu clang-format version 18.1.8 (distribution)\n")
        ToolVersions.verify(processes.operations, "clang-format", "clang-format", "18.1.8", prefix)
        val commands: List<List<String>> = processes.commands
        assertEquals(listOf(listOf("clang-format", VERSION_OPTION)), commands)

        val reports: List<String> = listOf("clang-format version 18.1.7", "clang-format version 18.1.80", "clang version 18.1.8")
        val messages: Map<String, String?> = reports.associateWith { reported ->
            assertThrows(GradleException::class.java) {
                ToolVersions.verify(RecordedProcesses(reported).operations, "clang-format", "clang-format", "18.1.8", prefix)
            }.message
        }
        val expected: Map<String, String?> =
            reports.associateWith { "clang-format reports $it, which is not the clang-format 18.1.8 this build pins!" }
        assertEquals(expected, messages)
    }

    @Test
    fun shouldAcceptTheBareZigVersionAndRejectOtherVersions() {
        val processes = RecordedProcesses("0.16.0\n")
        ToolVersions.verify(processes.operations, File(ZIG_TOOL), ZIG_TOOL, "0.16.0", prefix = "", argument = "version")
        val commands: List<List<String>> = processes.commands
        assertEquals(listOf(listOf(ZIG_TOOL, "version")), commands)

        val reports: List<String> = listOf("0.15.2", "0.16.0-dev.1+abc")
        val messages: Map<String, String?> = reports.associateWith { reported ->
            assertThrows(GradleException::class.java) {
                val operations: ExecOperations = RecordedProcesses(reported).operations
                ToolVersions.verify(operations, File(ZIG_TOOL), ZIG_TOOL, "0.16.0", prefix = "", argument = "version")
            }.message
        }
        val expected: Map<String, String?> = reports.associateWith { "zig reports $it, which is not the zig 0.16.0 this build pins!" }
        assertEquals(expected, messages)
    }
}
