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
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class NodeInstallationTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun everyModuleFormatsAndTestsWithTheOneNodeTheRootInstalls() {
        val project = directory.toFile()
        project.resolve("settings.gradle").writeText("rootProject.name = 'node-fixture'\ninclude 'first', 'second'\n")
        Path.of("..", "build.gradle.kts").toFile().copyTo(project.resolve("build.gradle.kts"))
        Path.of("..", "gradle", "libs.versions.toml").toFile().copyTo(project.resolve("gradle/libs.versions.toml"))
        for (name in listOf("first", "second")) {
            val module = project.resolve(name)
            module.mkdirs()
            module.resolve("build.gradle").writeText("plugins { id 'mcav.formatting'; id 'mcav.script-testing' }\n")
        }
        val result = GradleRunner.create()
            .withProjectDir(project)
            .withPluginClasspath()
            .withArguments("spotlessJava", "test", "--dry-run", "--stacktrace")
            .build()
        assertEquals(listOf(":nodeSetup SKIPPED"), result.output.lines().filter { it.endsWith("nodeSetup SKIPPED") })
    }
}
