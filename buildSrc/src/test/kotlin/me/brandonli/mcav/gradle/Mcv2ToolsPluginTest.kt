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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class Mcv2ToolsPluginTest {

    private companion object {

        const val REPOSITORY_DIRECTORY: String = ".."
        const val KOTLIN_BUILD_SCRIPT: String = "build.gradle.kts"
        const val CATALOG_PATH: String = "gradle/libs.versions.toml"

        const val SETTINGS_SCRIPT: String = "settings.gradle"
        const val BUILD_SCRIPT: String = "build.gradle"
        const val TOOLS_PROJECT: String = "tools"
    }

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldWriteTheClasspathThatMcv2ToolsTestStartsTheToolsWithBeforeMutationTesting(): Unit {
        val project: File = this.directory.toFile()
        project.resolve(SETTINGS_SCRIPT).writeText("rootProject.name = 'mcv2-tools-fixture'\ninclude 'tools'\n")
        Path.of(REPOSITORY_DIRECTORY, KOTLIN_BUILD_SCRIPT).toFile().copyTo(project.resolve(KOTLIN_BUILD_SCRIPT))
        Path.of(REPOSITORY_DIRECTORY, CATALOG_PATH).toFile().copyTo(project.resolve(CATALOG_PATH))
        val module: File = project.resolve(TOOLS_PROJECT)
        module.mkdirs()
        module.resolve(BUILD_SCRIPT).writeText("plugins { id 'mcav.mutation'; id 'mcav.mcv2-tools' }\n")
        val result: BuildResult = GradleRunner.create()
            .withProjectDir(project)
            .withPluginClasspath()
            .withArguments(":tools:pitest", "--dry-run", "--stacktrace")
            .build()
        val classpathTasks: List<String> = result.output.lines().filter { it.contains("writeMcv2ToolsClasspath") }
        assertEquals(listOf(":tools:writeMcv2ToolsClasspath SKIPPED"), classpathTasks)
    }
}
