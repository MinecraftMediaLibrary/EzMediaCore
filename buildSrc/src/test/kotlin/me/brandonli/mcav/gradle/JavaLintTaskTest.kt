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
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class JavaLintTaskTest {

    private companion object {

        const val LOGS_TASK: String = "logs"
        const val QUALIFIED_TASK: String = "qualified"
        const val VARIABLES_TASK: String = "variables"

        const val SOURCE_FILE: String = "Example.java"
        const val REPORT_EXTENSION: String = ".txt"
        const val VARIABLES_REPORT: String = "variables.txt"
    }

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldKeepTheExactSuccessReportOfEachLint(): Unit {
        val project: Project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val source: File = directory.resolve(SOURCE_FILE).toFile()
        source.writeText("final class Example {}\n")
        val tasks: List<Pair<JavaLintTask, String>> = listOf(
            project.tasks.register(LOGS_TASK, LogMessagesTask::class.java).get() to "no log call with a concatenated message in 1 files\n",
            project.tasks.register(QUALIFIED_TASK, QualifiedNamesTask::class.java).get() to "no fully qualified type names in 1 files\n",
            project.tasks.register(VARIABLES_TASK, VariableNamesTask::class.java).get() to "no undescriptive variable name in 1 files\n"
        )
        val reports: List<String> = tasks.map { (task, _) ->
            task.sources.from(source)
            task.report.set(directory.resolve(task.name + REPORT_EXTENSION).toFile())
            if (task is VariableNamesTask) task.publishedNames.set(emptySet())
            task.check()
            task.report.get().asFile.readText()
        }
        assertEquals(tasks.map { it.second }, reports)
    }

    @Test
    fun shouldCountTheViolationsAndKeepTheFailureMessageOfEachLint(): Unit {
        val project: Project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val tasks: List<Triple<JavaLintTask, String, String>> = listOf(
            Triple(
                project.tasks.register(LOGS_TASK, LogMessagesTask::class.java).get(),
                "final class Example { void run() { logger.info(\"joined \" + name); logger.error(\"joined \" + name); } }",
                "Found 2 log calls with a concatenated message!"
            ),
            Triple(
                project.tasks.register(QUALIFIED_TASK, QualifiedNamesTask::class.java).get(),
                "final class Example { java.time.Instant first; java.time.Instant second; }",
                "Found 2 fully qualified type names in Java code!"
            ),
            Triple(
                project.tasks.register(VARIABLES_TASK, VariableNamesTask::class.java).get(),
                "final class Example { int x; int y; }",
                "Found 2 undescriptive variable names!"
            )
        )
        val outcomes: List<Pair<String?, Boolean>> = tasks.map { (task, input, _) ->
            val source: File = directory.resolve(task.name + ".java").toFile()
            source.writeText(input)
            task.sources.from(source)
            task.report.set(directory.resolve(task.name + REPORT_EXTENSION).toFile())
            if (task is VariableNamesTask) task.publishedNames.set(emptySet())
            val failure: GradleException = assertThrows(GradleException::class.java) { task.check() }
            failure.message to task.report.get().asFile.exists()
        }
        val expected: List<Pair<String?, Boolean>> = tasks.map { it.third to false }
        assertEquals(expected, outcomes)
    }

    @Test
    fun shouldKeepAPublishedNameWhileTheOtherNamesFail(): Unit {
        val project: Project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        val task: VariableNamesTask = project.tasks.register(VARIABLES_TASK, VariableNamesTask::class.java).get()
        val source: File = directory.resolve(SOURCE_FILE).toFile()
        source.writeText("package me.example;\nfinal class Example { int x; int y; }\n")
        task.sources.from(source)
        task.report.set(directory.resolve(VARIABLES_REPORT).toFile())
        task.publishedNames.set(setOf("me.example.Example#x"))
        val failure: GradleException = assertThrows(GradleException::class.java) { task.check() }
        val message: String? = failure.message
        assertEquals("Found 1 undescriptive variable names!", message)
    }
}
