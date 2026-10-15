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
import java.util.Properties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CatalogPinsTest {

    private companion object {

        const val REPOSITORY_DIRECTORY: String = ".."
        const val CATALOG_PATH: String = "gradle/libs.versions.toml"
        const val TOOL_CHECKSUMS_PATH: String = "buildSrc/src/main/resources/tool-checksums.properties"

        const val READ_THE_DOCS_CONFIG: String = ".readthedocs.yaml"

        val PYTHON_MINOR_VERSION: Regex = Regex("(?m)^\\s+python: \"([0-9]+\\.[0-9]+)\"")
    }

    private val repository: File = Path.of(REPOSITORY_DIRECTORY).toFile()

    private val catalog: String = repository.resolve(CATALOG_PATH).readText()

    private fun version(name: String): String = Regex("(?m)^$name = \"([^\"]+)\"").find(catalog)!!.groupValues[1]

    @Test
    fun shouldBuildReadTheDocsWithThePythonMinorOfTheCatalog(): Unit {
        val readTheDocs: String = repository.resolve(READ_THE_DOCS_CONFIG).readText()
        val minor: String = PYTHON_MINOR_VERSION.find(readTheDocs)!!.groupValues[1]
        val catalogMinor: String = version("python").split('.').take(2).joinToString(".")
        assertEquals(catalogMinor, minor)
    }

    @Test
    fun shouldPinTheSixHostArchivesOfTheCatalogZigAndUvAndNothingElse(): Unit {
        val zig: String = version("zig")
        val uv: String = version("uv")
        val archives: List<String> = listOf("Linux", "Windows 11", "Mac OS X").flatMap { system ->
            listOf("amd64", "arm64").map { HostPlatform.normalize(system, it) }
        }.flatMap { host ->
            listOf(
                "zig/$zig/zig-${host.architecture}-${host.zigSystem}-$zig" + if (host.isWindows) ".zip" else ".tar.xz",
                "uv/$uv/uv-${host.uvTarget}" + if (host.isWindows) ".zip" else ".tar.gz"
            )
        }
        val pins: Properties = Properties()
        repository.resolve(TOOL_CHECKSUMS_PATH).inputStream().use { pins.load(it) }
        val pinnedArchives: Set<String> = pins.stringPropertyNames()
        assertEquals(archives.toSet(), pinnedArchives)
    }
}
