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
import org.gradle.api.GradleException
import org.gradle.process.ExecOperations
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ClangFormatTest {

    @TempDir
    lateinit var directory: Path

    abstract class RecordingFormat : ClangFormat() {
        lateinit var recordedProcesses: ExecOperations
        override val processes: ExecOperations get() = recordedProcesses
    }

    @Test
    fun theCheckFailsOnEveryUnformattedLineWithoutChangingTheSources() {
        val processes = RecordedProcesses("clang-format version 18.1.8\n")
        format(false, processes)
        assertEquals(
            listOf(listOf(clangFormat(), "--version"), listOf(clangFormat(), "--dry-run", "--Werror", "--style=file") + sources()),
            processes.commands
        )
    }

    @Test
    fun theFormatterRewritesTheSourcesInPlace() {
        val processes = RecordedProcesses("clang-format version 18.1.8\n")
        format(true, processes)
        val expected = listOf(listOf(clangFormat(), "--version"), listOf(clangFormat(), "-i", "--style=file") + sources())
        assertEquals(expected, processes.commands)
    }

    @Test
    fun anotherClangFormatVersionTouchesNoSource() {
        val processes = RecordedProcesses("Ubuntu clang-format version 19.1.1\n")
        val failure = assertThrows(GradleException::class.java) { format(true, processes) }
        assertEquals("${clangFormat()} must be clang-format 18.1.8, but it is Ubuntu clang-format version 19.1.1", failure.message)
        assertEquals(listOf(listOf(clangFormat(), "--version")), processes.commands)
    }

    private fun clangFormat(): String = directory.resolve("clang-format").toString()

    private fun sources(): List<String> = listOf("a.cpp", "b.cpp").map { directory.resolve(it).toString() }

    private fun format(rewrite: Boolean, processes: RecordedProcesses) {
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val task = project.tasks.register("format", RecordingFormat::class.java).get()
        val files = listOf("b.cpp", "a.cpp").map { directory.resolve(it).toFile() }
        files.forEach { it.writeText("int main() {}\n") }
        task.clangFormat.set(directory.resolve("clang-format").toFile())
        task.clangFormatVersion.set("18.1.8")
        task.sources.from(files)
        task.rewrite.set(rewrite)
        task.recordedProcesses = processes.operations
        task.format()
    }
}
