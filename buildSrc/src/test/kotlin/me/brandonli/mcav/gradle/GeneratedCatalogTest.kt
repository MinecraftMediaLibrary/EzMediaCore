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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GeneratedCatalogTest {

    private companion object {

        const val REPOSITORY_DIRECTORY: String = ".."
        const val CATALOG_PATH: String = "gradle/libs.versions.toml"
        const val GENERATED_SOURCE_PATH: String = "build/generated/sources/catalog/kotlin/me/brandonli/mcav/gradle/CatalogVersions.kt"

        const val HEADER_FILE: String = "HEADER"

        val JACOCO_VERSION: Regex = Regex("(?m)^jacoco = \"([^\"]+)\"")
    }

    @Test
    fun shouldKeepThePinOfTheGeneratedCatalogAndOpenItWithTheRepositoryLicense() {
        val repository = Path.of(REPOSITORY_DIRECTORY).toFile()
        val source = Path.of(GENERATED_SOURCE_PATH).toFile().readText()
        val licensed: Boolean = source.startsWith(repository.resolve(HEADER_FILE).readText())
        assertTrue(licensed)

        val catalog = repository.resolve(CATALOG_PATH).readText()
        val version = JACOCO_VERSION.find(catalog)!!.groupValues[1]
        val generatedVersion: String = CatalogVersions.JACOCO
        assertEquals(version, generatedVersion)

        val header: String = source.substringBefore("package").trimEnd()
        assertEquals(repository.resolve(HEADER_FILE).readText().trimEnd(), header)
    }
}
