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
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class PublishingPluginTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldNameTheProjectItsLicenseAndItsSourceRepositoryInThePublishedPom() {
        prepareProject()
        run("generatePomFileForMavenPublication")
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(directory.resolve("build/publications/maven/pom-default.xml").toFile())
        val project = document.documentElement
        fun text(tag: String): String = project.getElementsByTagName(tag).item(0)?.textContent ?: ""

        val license = project.getElementsByTagName("license").item(0)
        val licenseName: String? = license?.childNodes?.let { children ->
            (0 until children.length).map { children.item(it) }.single { it.nodeName == "name" }.textContent
        }
        val names: Map<String, String?> = mapOf(
            "groupId" to text("groupId"),
            "artifactId" to text("artifactId"),
            "version" to text("version"),
            "license" to licenseName,
            "connection" to text("connection"),
            "name" to text("name")
        )
        val expected: Map<String, String?> = mapOf(
            "groupId" to "me.brandonli",
            "artifactId" to "mcav-fixture",
            "version" to "1.2.3",
            "license" to "GNU General Public License, version 3 or later",
            "connection" to "scm:git:https://github.com/PulseBeat02/mcav.git",
            "name" to "mcav-fixture"
        )
        assertEquals(expected, names)
    }

    @Test
    fun shouldCarryTheProjectLicenseAndThirdPartyNoticesInEveryPublishedJar() {
        prepareProject()
        run("jar", "sourcesJar", "javadocJar")
        val classifiers: List<String> = listOf("", "-sources", "-javadoc")
        val legalTexts: List<String> = listOf("LICENSE", "THIRD-PARTY-NOTICES.md").map { directory.resolve(it).toFile().readText() }
        val jars: Map<String, List<String?>> = classifiers.associateWith { readLegalEntries(it) }
        val expected: Map<String, List<String?>> = classifiers.associateWith { legalTexts }
        assertEquals(expected, jars)
    }

    private fun readLegalEntries(classifier: String): List<String?> =
        ZipFile(directory.resolve("build/libs/mcav-fixture-1.2.3$classifier.jar").toFile()).use { jar ->
            listOf("META-INF/LICENSE-MCAV", "META-INF/THIRD-PARTY-NOTICES.md").map { name ->
                jar.getEntry(name)?.let { jar.getInputStream(it).reader().readText() }
            }
        }

    @Test
    fun shouldLetTheMutationTestJvmReadTheGeneratedPublication() {
        prepareProject()
        val catalog = directory.resolve("gradle/libs.versions.toml").toFile()
        catalog.parentFile.mkdirs()
        Path.of("..", "gradle", "libs.versions.toml").toFile().copyTo(catalog)
        directory.resolve("build.gradle").toFile().appendText(
            """
            apply plugin: 'mcav.mutation'
            tasks.register('readMutationPom', JavaExec) {
                dependsOn 'classes', 'generatePomFileForMavenPublication'
                classpath = sourceSets.main.runtimeClasspath
                mainClass = 'Example'
                doFirst { jvmArgs project.pitest.jvmArgs.get() }
            }
            """.trimIndent()
        )
        directory.resolve("src/main/java/Example.java").toFile().writeText(
            """
            import java.nio.file.Files;
            import java.nio.file.Path;

            public class Example {
                public static void main(String[] args) throws Exception {
                    String pom = System.getProperty("mcav.published.pom");
                    if (pom == null) {
                        throw new IllegalStateException("PIT's JVM cannot see the generated publication POM");
                    }
                    String contents = Files.readString(Path.of(pom));
                    if (!contents.contains("<artifactId>mcav-fixture</artifactId>")) {
                        throw new IllegalStateException("PIT's JVM must read this project's generated POM");
                    }
                    Files.writeString(Path.of("build/mutation-pom.txt"), contents);
                }
            }
            """.trimIndent()
        )
        run("readMutationPom")
        val generated: String = directory.resolve("build/publications/maven/pom-default.xml").toFile().readText()
        val read: String = directory.resolve("build/mutation-pom.txt").toFile().readText()
        assertEquals(generated, read)
    }

    @Test
    fun shouldFollowTheRootProjectGroupInThePublishedGroup() {
        prepareProject()
        directory.resolve("build.gradle").toFile().appendText("group = 'fixture.changed'\n")
        run("generatePomFileForMavenPublication")
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(directory.resolve("build/publications/maven/pom-default.xml").toFile())
        val groupId: String = document.documentElement.getElementsByTagName("groupId").item(0).textContent
        assertEquals("fixture.changed", groupId)
    }

    private fun prepareProject() {
        val project = directory.toFile()
        project.resolve("settings.gradle").writeText("rootProject.name = 'mcav-fixture'\n")
        project.resolve("build.gradle").writeText("plugins { id 'mcav.publishing' }\ngroup = 'me.brandonli'\nversion = '1.2.3'\n")
        project.resolve("LICENSE").writeText("Fixture license: preserve the complete root file.\n")
        project.resolve("THIRD-PARTY-NOTICES.md").writeText("Fixture notices: preserve the complete root file.\n")
        val source = project.resolve("src/main/java/Example.java")
        source.parentFile.mkdirs()
        source.writeText("/** A public example for the documentation jar. */ public class Example {}\n")
    }

    private fun run(vararg tasks: String) {
        GradleRunner.create()
            .withProjectDir(directory.toFile())
            .withPluginClasspath()
            .withArguments(*tasks, "--max-workers=2", "--stacktrace")
            .build()
    }
}
