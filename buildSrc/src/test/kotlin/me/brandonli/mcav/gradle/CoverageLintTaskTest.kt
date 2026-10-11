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
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class CoverageLintTaskTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun aFilteredTestRunSkipsTheLint() {
        val task = lint(uncoveredLine = true)
        task.testsFiltered.set(true)
        task.lint()
    }

    @Test
    fun failsOnAnUncoveredLine() {
        val task = lint(uncoveredLine = true)
        val failure = assertThrows(GradleException::class.java) { task.lint() }
        assertEquals("1 lines are not fully covered by tests and 0 coverage exceptions are stale", failure.message)
    }

    @Test
    fun failsOnAnExceptionThatMatchesNoUncoveredLine() {
        val task = lint(uncoveredLine = false)
        directory.resolve("exceptions.txt").toFile().writeText("sample/Example.java | serverOnly(); | server constructor\n")
        val failure = assertThrows(GradleException::class.java) { task.lint() }
        assertEquals("0 lines are not fully covered by tests and 1 coverage exceptions are stale", failure.message)
    }

    @Test
    fun passesWhenAnExceptionExcusesTheOnlyUncoveredLine() {
        val task = lint(uncoveredLine = true)
        directory.resolve("exceptions.txt").toFile().writeText("sample/Example.java | serverOnly(); | server constructor\n")
        task.lint()
    }

    private fun lint(uncoveredLine: Boolean): CoverageLintTask {
        val source = directory.resolve("sources/sample/Example.java").toFile()
        source.parentFile.mkdirs()
        source.writeText("serverOnly();\n")
        val coverage = directory.resolve("report.xml").toFile()
        val counts = if (uncoveredLine) "mi=\"1\" ci=\"0\"" else "mi=\"0\" ci=\"1\""
        coverage.writeText(
            "<report name=\"fixture\"><package name=\"sample\"><sourcefile name=\"Example.java\">" +
                "<line nr=\"1\" $counts mb=\"0\" cb=\"0\"/></sourcefile></package></report>"
        )
        val project = ProjectBuilder.builder().withProjectDir(directory.toFile()).build()
        return project.tasks.register("coverageLint", CoverageLintTask::class.java).get().apply {
            report.from(coverage)
            sourceDirectory.set(directory.resolve("sources").toFile())
            exceptionsFile.from(directory.resolve("exceptions.txt").toFile())
            testsFiltered.set(false)
            projectPath.set(":fixture")
        }
    }
}
