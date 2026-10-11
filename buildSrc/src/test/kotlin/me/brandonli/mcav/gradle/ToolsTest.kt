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

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ToolsTest {

    @Test
    fun aPinnedArchiveHasItsChecksumAndAnUnpinnedOneNamesItsOverrideVariable() {
        val project = ProjectBuilder.builder().build()
        assertEquals(
            "70e49664a74374b48b51e6f3fdfbf437f6395d42509050588bd49abe52ba3d00",
            project.pinnedSha256("zig", "0.16.0", "zig-x86_64-linux-0.16.0.tar.xz").get()
        )
        val failure = assertThrows(GradleException::class.java) { project.pinnedSha256("clang-format", "1.0", "clang.zip").get() }
        assertEquals(
            "tool-checksums.properties pins no SHA-256 for clang-format/1.0/clang.zip; set CLANG_FORMAT=/path/to/clang-format 1.0",
            failure.message
        )
    }

    @Test
    fun withoutItsOverrideVariableTheInstalledToolIsSelected() {
        val project = ProjectBuilder.builder().build()
        val installed = project.layout.buildDirectory.file("tools/fixture-tool/1.0/fixture-tool")
        assertFalse(project.toolOverride("fixture-tool").isPresent)
        assertSame(installed, project.selectTool("fixture-tool", installed))
    }

    @Test
    fun aSetOverrideVariableSelectsItsFileOverTheInstalledTool() {
        val project = ProjectBuilder.builder().build()
        val installed = project.layout.buildDirectory.file("tools/path/1.0/path")
        // a test cannot set a variable of its own process, and PATH is always set
        val variable = project.file(System.getenv("PATH"))
        assertEquals(variable, project.toolOverride("path").get().asFile)
        assertEquals(variable, project.selectTool("path", installed).get().asFile)
    }
}
