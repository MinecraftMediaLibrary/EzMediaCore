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
import java.nio.file.Path
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.RETURNS_SELF
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.invocation.Invocation

class JupyterBookTest {

    private companion object {

        const val BOOK_CONFIG_FILE: String = "_config.yml"
        const val SPHINX_CONFIG_FILE: String = "conf.py"

        const val HTML_OUTPUT_PATH: String = "site/_build/html"
        const val INDEX_FILE: String = "index.html"
        const val EPUB_OUTPUT_PATH: String = "site/_build/epub"
        const val EPUB_FILE: String = "book.epub"

        const val SOURCE_DIRECTORY: String = "source"
        const val MANAGED_PYTHON_DIRECTORY: String = "managed-python"
        const val HTML_DIRECTORY: String = "html"
        const val BOOK_DIRECTORY: String = "book"
    }

    @TempDir
    lateinit var directory: Path

    abstract class RecordingBook : JupyterBook() {

        lateinit var recordedProcesses: ExecOperations
        override val processes: ExecOperations get() = this.recordedProcesses
    }

    @Test
    fun shouldGenerateTheConfigurationBeforeTheHtmlBuildAndLetOnlyHtmlLeaveTheTask(): Unit {
        val project: Project = ProjectBuilder.builder().withProjectDir(this.directory.toFile()).build()
        val task: RecordingBook = project.tasks.register(BOOK_DIRECTORY, RecordingBook::class.java).get()
        val source: File = this.directory.resolve(SOURCE_DIRECTORY).toFile()
        source.mkdirs()
        source.resolve(BOOK_CONFIG_FILE).writeText("title: Fixture book\n")
        source.resolve(SPHINX_CONFIG_FILE).writeText("html_title = 'Stale title'\n")
        task.sources.from(project.fileTree(source))
        task.pythonExecutable.set(this.directory.resolve(MANAGED_PYTHON_DIRECTORY).toFile())
        task.outputDirectory.set(this.directory.resolve(HTML_DIRECTORY).toFile())
        val commands: MutableList<List<String>> = mutableListOf<List<String>>()
        val processes: ExecOperations = mock(ExecOperations::class.java)
        val expectedConfiguration: String = "html_title = 'Fixture book'\n"
        val expectedHtml: String = "<!doctype html><title>Fixture book</title>\n"
        doAnswer { invocation ->
            val specification: ExecSpec = mock(ExecSpec::class.java, RETURNS_SELF)
            invocation.getArgument<Action<ExecSpec>>(0).execute(specification)
            val calls: Collection<Invocation> = mockingDetails(specification).invocations
            val command: List<String> =
                (calls.single { it.method.name == "commandLine" }.rawArguments[0] as Iterable<*>).map { it.toString() }
            val staging: File = calls.single { it.method.name == "workingDir" }.rawArguments[0] as File
            val arguments: List<String> = command.drop(3)
            commands += arguments
            val book: File = staging.resolve(BOOK_DIRECTORY)
            val stagedConfiguration: String = book.resolve(BOOK_CONFIG_FILE).readText()
            assertEquals("title: Fixture book\n", stagedConfiguration)
            if (arguments.first() == "config") {
                book.resolve(SPHINX_CONFIG_FILE).writeText(expectedConfiguration)
            } else {
                val generatedConfiguration: String = book.resolve(SPHINX_CONFIG_FILE).readText()
                assertEquals(expectedConfiguration, generatedConfiguration)
                val html: File = staging.resolve(HTML_OUTPUT_PATH)
                html.mkdirs()
                html.resolve(INDEX_FILE).writeText(expectedHtml)
                val epub: File = staging.resolve(EPUB_OUTPUT_PATH)
                epub.mkdirs()
                epub.resolve(EPUB_FILE).writeText("not an HTML output")
            }
            mock(ExecResult::class.java)
        }.`when`(processes).exec(any())
        task.recordedProcesses = processes
        task.build()
        assertEquals(listOf(
            listOf("config", "sphinx", BOOK_DIRECTORY),
            listOf("build", BOOK_DIRECTORY, "--path-output", "site", "--warningiserror", "--keep-going", "--all")
        ), commands)

        val output: File = task.outputDirectory.get().asFile
        val html: String = output.resolve(INDEX_FILE).readText()
        assertEquals(expectedHtml, html)

        val configurationLeft: Boolean = output.resolve(SPHINX_CONFIG_FILE).exists()
        assertFalse(configurationLeft)

        val epubLeft: Boolean = output.resolve(EPUB_FILE).exists()
        assertFalse(epubLeft)
    }
}
