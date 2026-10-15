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
import java.util.zip.ZipFile
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class NativeResourcesTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldBuildAndPackageTheGeneratedResourceArtifactIntoAShadowConsumer() {
        val project = directory.toFile()
        project.resolve("settings.gradle").writeText("rootProject.name = 'native-resources'\ninclude 'producer', 'consumer'\n")
        project.resolve("build.gradle").writeText("plugins { id 'mcav.python' }\n")
        val catalog = project.resolve("gradle/libs.versions.toml")
        catalog.parentFile.mkdirs()
        Path.of("..", "gradle", "libs.versions.toml").toFile().copyTo(catalog)
        val producer = project.resolve("producer").apply { mkdirs() }
        val sources = producer.resolve("src/main/native/mcv2").apply { mkdirs() }
        sources.resolve("mcv2.cpp").writeText("native fixture bytes")
        val compiler = project.resolve("zig").apply { writeText("unused fixture compiler") }
        producer.resolve("build.gradle").writeText("""
            plugins { id 'mcav.natives' }
            tasks.named('buildMcv2Natives') {
                actions.clear()
                doLast {
                    def library = outputDirectory.file('mcav/mcv2/natives/linux-x86_64/libmcv2kernels.so').get().asFile
                    library.parentFile.mkdirs()
                    library.bytes = sourcesDirectory.file('mcv2.cpp').get().asFile.bytes
                }
            }
        """.trimIndent())
        val consumer = project.resolve("consumer").apply { mkdirs() }
        consumer.resolve("build.gradle").writeText("""
            plugins {
                id 'java'
                id 'com.gradleup.shadow'
            }
            configurations {
                natives { canBeConsumed = false; canBeResolved = true }
            }
            dependencies {
                natives project(path: ':producer', configuration: 'mcv2NativeResources')
            }
            tasks.named('shadowJar') { from(project.configurations.natives) }
        """.trimIndent())
        val runner = GradleRunner.create().withProjectDir(project).withPluginClasspath()
            .withEnvironment(System.getenv() + ("ZIG" to compiler.absolutePath))
            .withArguments(":consumer:shadowJar", "--stacktrace")
        val first = runner.build()
        val download: BuildTask? = first.task(":producer:downloadZig")
        assertNull(download)

        val firstOutcome: TaskOutcome = first.task(":producer:buildMcv2Natives")!!.outcome
        assertEquals(TaskOutcome.SUCCESS, firstOutcome)

        val packaged: String? = readLibrary(consumer)
        assertEquals("native fixture bytes", packaged)

        val unchangedOutcome: TaskOutcome = runner.build().task(":producer:buildMcv2Natives")!!.outcome
        assertEquals(TaskOutcome.UP_TO_DATE, unchangedOutcome)

        sources.resolve("mcv2.cpp").writeText("changed native fixture bytes")
        val changedOutcome: TaskOutcome = runner.build().task(":producer:buildMcv2Natives")!!.outcome
        assertEquals(TaskOutcome.SUCCESS, changedOutcome)

        val repackaged: String? = readLibrary(consumer)
        assertEquals("changed native fixture bytes", repackaged)
    }

    private fun readLibrary(consumer: File): String? =
        ZipFile(consumer.resolve("build/libs/consumer-all.jar")).use { archive ->
            archive.getEntry("mcav/mcv2/natives/linux-x86_64/libmcv2kernels.so")
                ?.let { archive.getInputStream(it).reader().readText() }
        }
}
