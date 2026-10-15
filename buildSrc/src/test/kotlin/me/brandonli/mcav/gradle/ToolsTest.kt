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
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ToolsTest {

    private companion object {

        const val CLANG_ARCHIVE: String = "clang.zip"
        const val FIXTURE_TOOL_PATH: String = "tools/fixture-tool/1.0/fixture-tool"
        const val PATH_TOOL_PATH: String = "tools/path/1.0/path"
    }

    @Test
    fun shouldGiveAPinnedArchiveItsChecksumAndNameTheOverrideVariableOfAnUnpinnedOne() {
        val project = ProjectBuilder.builder().build()
        val checksum: String = project.pinnedSha256("zig", "0.16.0", "zig-x86_64-linux-0.16.0.tar.xz").get()
        assertEquals("70e49664a74374b48b51e6f3fdfbf437f6395d42509050588bd49abe52ba3d00", checksum)

        val failure = assertThrows(GradleException::class.java) { project.pinnedSha256("clang-format", "1.0", CLANG_ARCHIVE).get() }
        val message: String? = failure.message
        val expected: String =
            "tool-checksums.properties pins no SHA-256 for clang-format/1.0/clang.zip, so set CLANG_FORMAT to the path of clang-format 1.0!"
        assertEquals(expected, message)
    }

    @Test
    fun shouldSelectTheInstalledToolWithoutItsOverrideVariable() {
        val project = ProjectBuilder.builder().build()
        val installed = project.layout.buildDirectory.file(FIXTURE_TOOL_PATH)
        val overridden: Boolean = project.toolOverride("fixture-tool").isPresent
        assertFalse(overridden)

        val selected: Provider<RegularFile> = project.selectTool("fixture-tool", installed)
        assertSame(installed, selected)
    }

    @Test
    fun shouldSelectTheFileOfASetOverrideVariableOverTheInstalledTool() {
        val project = ProjectBuilder.builder().build()
        val installed = project.layout.buildDirectory.file(PATH_TOOL_PATH)
        // a test cannot set a variable of its own process, and PATH is always set
        val variable = project.file(System.getenv("PATH"))
        val override: File = project.toolOverride("path").get().asFile
        assertEquals(variable, override)

        val selected: File = project.selectTool("path", installed).get().asFile
        assertEquals(variable, selected)
    }
}
