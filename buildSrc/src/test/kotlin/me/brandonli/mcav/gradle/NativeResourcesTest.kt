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
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class NativeResourcesTest {

    private companion object {

        const val REPOSITORY_DIRECTORY: String = ".."
        const val CATALOG_PATH: String = "gradle/libs.versions.toml"

        const val NATIVE_SOURCES_PATH: String = "src/main/native/mcv2"
        const val NATIVE_SOURCE: String = "mcv2.cpp"
        const val CONSUMER_JAR_PATH: String = "build/libs/consumer-all.jar"
        const val NATIVE_LIBRARY_ENTRY: String = "mcav/mcv2/natives/linux-x86_64/libmcv2kernels.so"

        const val SETTINGS_SCRIPT: String = "settings.gradle"
        const val BUILD_SCRIPT: String = "build.gradle"
        const val PRODUCER_PROJECT: String = "producer"
        const val CONSUMER_PROJECT: String = "consumer"
        const val ZIG_EXECUTABLE: String = "zig"
    }

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldBuildAndPackageTheGeneratedResourceArtifactIntoAShadowConsumer(): Unit {
        val project: File = directory.toFile()
        project.resolve(SETTINGS_SCRIPT).writeText("rootProject.name = 'native-resources'\ninclude 'producer', 'consumer'\n")
        project.resolve(BUILD_SCRIPT).writeText("plugins { id 'mcav.python' }\n")
        val catalog: File = project.resolve(CATALOG_PATH)
        catalog.parentFile.mkdirs()
        Path.of(REPOSITORY_DIRECTORY, CATALOG_PATH).toFile().copyTo(catalog)
        val producer: File = project.resolve(PRODUCER_PROJECT).apply { mkdirs() }
        val sources: File = producer.resolve(NATIVE_SOURCES_PATH).apply { mkdirs() }
        sources.resolve(NATIVE_SOURCE).writeText("native fixture bytes")
        val compiler: File = project.resolve(ZIG_EXECUTABLE).apply { writeText("unused fixture compiler") }
        producer.resolve(BUILD_SCRIPT).writeText("""
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
        val consumer: File = project.resolve(CONSUMER_PROJECT).apply { mkdirs() }
        consumer.resolve(BUILD_SCRIPT).writeText("""
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
        val runner: GradleRunner = GradleRunner.create().withProjectDir(project).withPluginClasspath()
            .withEnvironment(System.getenv() + ("ZIG" to compiler.absolutePath))
            .withArguments(":consumer:shadowJar", "--stacktrace")
        val first: BuildResult = runner.build()
        val download: BuildTask? = first.task(":producer:downloadZig")
        assertNull(download)

        val firstOutcome: TaskOutcome = first.task(":producer:buildMcv2Natives")!!.outcome
        assertEquals(TaskOutcome.SUCCESS, firstOutcome)

        val packaged: String? = readLibrary(consumer)
        assertEquals("native fixture bytes", packaged)

        val unchangedOutcome: TaskOutcome = runner.build().task(":producer:buildMcv2Natives")!!.outcome
        assertEquals(TaskOutcome.UP_TO_DATE, unchangedOutcome)

        sources.resolve(NATIVE_SOURCE).writeText("changed native fixture bytes")
        val changedOutcome: TaskOutcome = runner.build().task(":producer:buildMcv2Natives")!!.outcome
        assertEquals(TaskOutcome.SUCCESS, changedOutcome)

        val repackaged: String? = readLibrary(consumer)
        assertEquals("changed native fixture bytes", repackaged)
    }

    private fun readLibrary(consumer: File): String? =
        ZipFile(consumer.resolve(CONSUMER_JAR_PATH)).use { archive ->
            archive.getEntry(NATIVE_LIBRARY_ENTRY)
                ?.let { archive.getInputStream(it).reader().readText() }
        }
}
