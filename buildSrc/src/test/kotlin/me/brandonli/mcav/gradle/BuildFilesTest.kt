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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuildFilesTest {

    private val root = File("..")

    private val modules = Regex("""^ {4}"([\w-]+)",$""", RegexOption.MULTILINE)
        .findAll(root.resolve("settings.gradle.kts").readText())
        .map { it.groupValues[1] }
        .toList()

    private val moduleScripts = modules.map { root.resolve("$it/build.gradle.kts") }

    private val buildScripts = listOf(
        "settings.gradle.kts",
        "build.gradle.kts",
        "buildSrc/settings.gradle.kts",
        "buildSrc/build.gradle.kts"
    ).map { root.resolve(it) } + moduleScripts

    private val sources = File("src").walk().filter { it.isFile && (it.name.endsWith(".kt") || it.name.endsWith(".kts")) }.toList()

    @Test
    fun everySourceOfTheBuildOpensWithTheLicenseHeaderAndNoBuildScriptCarriesIt() {
        val header = root.resolve("HEADER").readText()
        sources.forEach { assertTrue(it.readText().startsWith("$header\n"), it.path) }
        buildScripts.forEach { assertFalse(it.readText().contains(header), it.path) }
    }

    @Test
    fun everyModuleScriptOnlyAppliesConventionsAndListsDependencies() {
        val layout = Regex("""((import [\w.]+\n)+\n)?plugins \{\n( {4}id\("mcav\.[\w-]+"\)\n)+}\n(\ndependencies \{\n( {4}.+\n)+}\n)?""")
        moduleScripts.forEach { assertTrue(layout.matches(it.readText()), it.path) }
    }

    @Test
    fun settingsIncludeEveryModuleInOrderAndEverySectionOfTheCatalogIsSorted() {
        val directories = root.listFiles { file -> file.name != "buildSrc" && file.resolve("build.gradle.kts").isFile }!!
        assertEquals(directories.map { it.name }.sorted(), modules)
        val sections = root.resolve("gradle/libs.versions.toml").readText().split(Regex("""^\[\w+]\n""", RegexOption.MULTILINE))
        sections.drop(1).forEach { section ->
            val names = Regex("""^([\w-]+) = """, RegexOption.MULTILINE).findAll(section).map { it.groupValues[1] }.toList()
            assertEquals(names.sorted(), names)
        }
    }

    @Test
    fun noLineOfTheBuildIsLongerThan140Columns() {
        (sources + buildScripts).forEach { file ->
            file.readLines().forEachIndexed { index, line -> assertTrue(line.length <= 140, "${file.path}:${index + 1}") }
        }
    }
}
