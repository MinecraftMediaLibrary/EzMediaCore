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
import java.util.Properties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CatalogPinsTest {

    private val repository = Path.of("..").toFile()

    private val catalog = repository.resolve("gradle/libs.versions.toml").readText()

    private fun version(name: String): String = Regex("(?m)^$name = \"([^\"]+)\"").find(catalog)!!.groupValues[1]

    @Test
    fun readTheDocsUsesTheCatalogPythonMinor() {
        val readTheDocs = repository.resolve(".readthedocs.yaml").readText()
        val minor = Regex("(?m)^\\s+python: \"([0-9]+\\.[0-9]+)\"").find(readTheDocs)!!.groupValues[1]
        assertEquals(version("python").split('.').take(2).joinToString("."), minor)
    }

    @Test
    fun theToolChecksumsPinTheSixHostArchivesOfTheCatalogZigAndUvAndNothingElse() {
        val zig = version("zig")
        val uv = version("uv")
        val archives = listOf("Linux", "Windows 11", "Mac OS X").flatMap { system ->
            listOf("amd64", "arm64").map { HostPlatform.normalize(system, it) }
        }.flatMap { host ->
            listOf(
                "zig/$zig/zig-${host.architecture}-${host.zigSystem}-$zig" + if (host.isWindows) ".zip" else ".tar.xz",
                "uv/$uv/uv-${host.uvTarget}" + if (host.isWindows) ".zip" else ".tar.gz"
            )
        }
        val pins = Properties()
        repository.resolve("buildSrc/src/main/resources/tool-checksums.properties").inputStream().use { pins.load(it) }
        assertEquals(archives.toSet(), pins.stringPropertyNames())
    }
}
