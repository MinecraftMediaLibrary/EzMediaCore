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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class CheckerStubsTest {

    private companion object {

        const val BUKKIT_PROJECT: String = "mcav-bukkit"
        const val MOD_PROJECT: String = "mcav-mod"

        const val GUAVA_STUBS: String = "shared-guava"
    }

    @TempDir
    lateinit var directory: Path

    @Test
    fun shouldGiveTheSharedGuavaAndPaperStubsOnlyToTheirConsumers(): Unit {
        val projects: List<String> = listOf("mcav-common", BUKKIT_PROJECT, "mcav-plugin", "mcav-browser", "mcav-discord", "mcav-http",
            "mcav-installer", "mcav-lwjgl", MOD_PROJECT, "mcav-vm", "mcav-vnc", "mcav-voicechat", "mcav-jcstress")
        (projects + listOf("shared", GUAVA_STUBS, "shared-paper")).forEach { directory.resolve(it).toFile().mkdirs() }
        val expected: Map<String, List<String>> = projects.associateWith { project ->
            when (project) {
                "mcav-common" -> listOf(project, "shared", GUAVA_STUBS)
                BUKKIT_PROJECT -> listOf(project, "shared", GUAVA_STUBS, "shared-paper")
                "mcav-plugin" -> listOf(project, "shared", "shared-paper")
                else -> listOf(project, "shared")
            }
        }
        val stubs: Map<String, List<String>> = projects.associateWith { project ->
            CheckerStubs.forProject(directory.toFile(), project).map { it.name }
        }
        assertEquals(expected, stubs)
    }

    @Test
    fun shouldNotPassStubFoldersThatDoNotExistToTheChecker(): Unit {
        val withoutFolders: List<String> = CheckerStubs.forProject(directory.toFile(), BUKKIT_PROJECT).map { it.name }
        assertEquals(emptyList<String>(), withoutFolders)

        directory.resolve(GUAVA_STUBS).toFile().mkdirs()
        val withGuava: List<String> = CheckerStubs.forProject(directory.toFile(), BUKKIT_PROJECT).map { it.name }
        assertEquals(listOf(GUAVA_STUBS), withGuava)

        val otherProject: List<String> = CheckerStubs.forProject(directory.toFile(), MOD_PROJECT).map { it.name }
        assertEquals(emptyList<String>(), otherProject)
    }
}
