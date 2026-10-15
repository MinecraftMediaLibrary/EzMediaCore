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

import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PaperVersionTest {

    @Test
    fun shouldSupplyTheExactServerBuildAndArtifactNameOfThePinnedAlphaVersion() {
        val version = PaperVersion.parse("26.3.build.49-alpha")
        val minecraft: String = version.minecraft
        assertEquals("26.3", minecraft)

        val build: Int = version.build
        assertEquals(49, build)

        val fileName: String = version.fileName
        assertEquals("paper-26.3-49.jar", fileName)

        val releaseFileName: String = PaperVersion.parse("26.4.build.123").fileName
        assertEquals("paper-26.4-123.jar", releaseFileName)
    }

    @Test
    fun shouldRejectAVersionWithoutAnExplicitBuild() {
        val versions: List<String> = listOf("26.3", "26.3.build.latest")
        val messages: Map<String, String?> = versions.associateWith { version ->
            assertThrows(GradleException::class.java) { PaperVersion.parse(version) }.message
        }
        val expected: Map<String, String?> = versions.associateWith { "Paper version must name a build, but it is $it" }
        assertEquals(expected, messages)
    }
}
