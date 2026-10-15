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
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class CoverageLintPluginTest {

    private companion object {

        const val SOURCE_PATH: String = "src/main/java/Example.java"

        const val SETTINGS_SCRIPT: String = "settings.gradle"
        const val BUILD_SCRIPT: String = "build.gradle"
    }

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldKeepTheCoverageOfAnotherModuleWhenOneModuleIsFiltered(): Unit {
        val project: File = this.directory.toFile()
        project.resolve(SETTINGS_SCRIPT).writeText("rootProject.name = 'coverage-fixture'\ninclude 'first', 'second'\n")
        for (name in listOf("first", "second")) {
            val module: File = project.resolve(name)
            module.mkdirs()
            module.resolve(BUILD_SCRIPT).writeText("plugins { id 'mcav.coverage' }\n")
            val source: File = module.resolve(SOURCE_PATH)
            source.parentFile.mkdirs()
            source.writeText("public class Example { public int value() { return 42; } }\n")
        }
        val result: BuildResult = GradleRunner.create()
            .withProjectDir(project)
            .withPluginClasspath()
            .withArguments(":first:test", "--tests", "ExampleTest", ":first:coverageLint", ":second:coverageLint", "--stacktrace")
            .buildAndFail()

        val output: String = result.output
        val firstSkipped: Boolean = output.contains("Skipped coverageLint of :first,")
        assertTrue(firstSkipped, output)

        val secondSkipped: Boolean = output.contains("Skipped coverageLint of :second,")
        assertFalse(secondSkipped, output)

        val uncovered: Boolean = output.contains("no test ran, so none of this code is covered.")
        assertTrue(uncovered, output)

        val secondFailed: Boolean = output.contains(":second:coverageLint FAILED")
        assertTrue(secondFailed, output)
    }
}
