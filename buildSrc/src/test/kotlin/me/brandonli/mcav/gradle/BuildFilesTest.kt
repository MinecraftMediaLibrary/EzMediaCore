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
    fun shouldOpenEverySourceOfTheBuildWithTheLicenseHeaderAndNoBuildScript() {
        val header = root.resolve("HEADER").readText()
        val sourcesWithoutHeader: List<String> = sources.filterNot { it.readText().startsWith("$header\n") }.map { it.path }
        assertEquals(listOf<String>(), sourcesWithoutHeader)

        val scriptsWithHeader: List<String> = buildScripts.filter { it.readText().contains(header) }.map { it.path }
        assertEquals(listOf<String>(), scriptsWithHeader)
    }

    @Test
    fun shouldOnlyApplyConventionsAndListDependenciesInEveryModuleScript() {
        val layout = Regex("""((import [\w.]+\n)+\n)?plugins \{\n( {4}id\("mcav\.[\w-]+"\)\n)+}\n(\ndependencies \{\n( {4}.+\n)+}\n)?""")
        val offenders: List<String> = moduleScripts.filterNot { layout.matches(it.readText()) }.map { it.path }
        assertEquals(listOf<String>(), offenders)
    }

    @Test
    fun shouldIncludeEveryModuleInOrderAndSortEverySectionOfTheCatalog() {
        val directories = root.listFiles { file -> file.name != "buildSrc" && file.resolve("build.gradle.kts").isFile }!!
        val sortedModules: List<String> = directories.map { it.name }.sorted()
        assertEquals(sortedModules, modules)

        val sections = root.resolve("gradle/libs.versions.toml").readText().split(Regex("""^\[\w+]\n""", RegexOption.MULTILINE))
        val names: List<List<String>> = sections.drop(1).map { section ->
            Regex("""^([\w-]+) = """, RegexOption.MULTILINE).findAll(section).map { it.groupValues[1] }.toList()
        }
        val sortedNames: List<List<String>> = names.map { it.sorted() }
        assertEquals(sortedNames, names)
    }

    @Test
    fun shouldKeepEveryLineOfTheBuildWithin140Columns() {
        val longLines: List<String> = (sources + buildScripts).flatMap { file ->
            file.readLines().withIndex().filter { it.value.length > 140 }.map { "${file.path}:${it.index + 1}" }
        }
        assertEquals(listOf<String>(), longLines)
    }
}
