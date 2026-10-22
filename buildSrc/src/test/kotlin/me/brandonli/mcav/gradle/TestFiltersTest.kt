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

import org.gradle.api.Project
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter
import org.gradle.api.tasks.testing.Test as GradleTest
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class TestFiltersTest {

    private companion object {

        const val TASK_NAME: String = "fixtureTest"

        const val TEST_PATTERN: String = "me.brandonli.mcav.FixtureTest"
        const val FILE_PATTERN: String = "**/FixtureTest*"
    }

    @Test
    fun shouldNotTreatATestTaskWithoutPatternsAsFiltered(): Unit {
        val test: GradleTest = this.createTestTask()

        val filtered: Boolean = TestFilters.isFiltered(test)
        assertFalse(filtered)
    }

    @Test
    fun shouldTreatEveryKindOfPatternAsAFilter(): Unit {
        val commandLine: GradleTest = this.createTestTask()
        val commandLineFilter: DefaultTestFilter = commandLine.filter as DefaultTestFilter
        commandLineFilter.setCommandLineIncludePatterns(listOf(TEST_PATTERN))

        val included: GradleTest = this.createTestTask()
        included.filter.includeTestsMatching(TEST_PATTERN)

        val excluded: GradleTest = this.createTestTask()
        excluded.filter.excludeTestsMatching(TEST_PATTERN)

        val includedFiles: GradleTest = this.createTestTask()
        includedFiles.include(FILE_PATTERN)

        val excludedFiles: GradleTest = this.createTestTask()
        excludedFiles.exclude(FILE_PATTERN)

        val tests: List<GradleTest> = listOf(commandLine, included, excluded, includedFiles, excludedFiles)

        val filtered: List<Boolean> = tests.map(TestFilters::isFiltered)
        val expected: List<Boolean> = listOf(true, true, true, true, true)
        assertEquals(expected, filtered)
    }

    private fun createTestTask(): GradleTest {
        val project: Project = ProjectBuilder.builder().build()
        return project.tasks.register(TASK_NAME, GradleTest::class.java).get()
    }
}
