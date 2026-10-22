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
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ExplicitLocalePluginTest {

    private companion object {

        const val REPOSITORY_DIRECTORY: String = ".."
        const val CATALOG_PATH: String = "gradle/libs.versions.toml"
        const val SOURCE_PATH: String = "src/main/java/fixture/NumericOutput.java"

        const val SETTINGS_SCRIPT: String = "settings.gradle"
        const val BUILD_SCRIPT: String = "build.gradle"
    }

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldRequireProductionNumberFormattingToChooseALocale(): Unit {
        val project: File = this.directory.toFile()
        project.resolve(SETTINGS_SCRIPT).writeText("rootProject.name = 'locale-fixture'\n")
        project.resolve(BUILD_SCRIPT).writeText("plugins { id 'mcav.java-library' }\n")

        val catalog: File = project.resolve(CATALOG_PATH)
        catalog.parentFile.mkdirs()
        Path.of(REPOSITORY_DIRECTORY, CATALOG_PATH).toFile().copyTo(catalog)

        val source: File = project.resolve(SOURCE_PATH)
        source.parentFile.mkdirs()
        source.writeText(
            """
            package fixture;
            public final class NumericOutput {
                private NumericOutput() { throw new UnsupportedOperationException(); }
                public static String render(final int value) {
                    return "%d".formatted(value);
                }
            }
            """.trimIndent()
        )

        val runner: GradleRunner = GradleRunner.create()
            .withProjectDir(project)
            .withPluginClasspath()
            .withArguments("compileJava", "--no-parallel", "--max-workers=1")
        val rejected: BuildResult = runner.buildAndFail()
        val flagged: Boolean = rejected.output.contains("error: [DefaultLocale]")
        assertTrue(flagged, rejected.output)

        source.writeText(
            """
            package fixture;
            import java.util.Locale;
            public final class NumericOutput {
                private NumericOutput() { throw new UnsupportedOperationException(); }
                public static String render(final int value) {
                    return String.format(Locale.ROOT, "%d", value);
                }
            }
            """.trimIndent()
        )

        val accepted: BuildResult = runner.build()
        val outcome: TaskOutcome? = accepted.task(":compileJava")?.outcome
        assertEquals(TaskOutcome.SUCCESS, outcome, accepted.output)
    }
}
