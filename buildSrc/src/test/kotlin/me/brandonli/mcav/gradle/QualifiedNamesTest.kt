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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class QualifiedNamesTest {

    @Test
    fun reportsAQualifiedNameInCode() {
        val source = """
            package me.example;

            final class Example {
              void run() {
                java.util.Arrays.fill(new int[1], 0);
              }
            }
        """.trimIndent()
        assertEquals(listOf(QualifiedName(5, "java.util.Arrays")), QualifiedNames.find(source))
    }

    @Test
    fun reportsEveryOccurrenceAndStopsAtTheOuterType() {
        val source = """
            final class Example {
              final java.util.Map.Entry<String, java.util.List<String>> entry = null;
            }
        """.trimIndent()
        assertEquals(listOf(QualifiedName(2, "java.util.Map"), QualifiedName(2, "java.util.List")), QualifiedNames.find(source))
    }

    @Test
    fun acceptsAClashMarkedWithItsReason() {
        val source = """
            import org.bukkit.entity.Display;

            final class Example {
              net.minecraft.world.entity.Display handle; // fqn: Display is imported as org.bukkit.entity.Display
            }
        """.trimIndent()
        assertEquals(emptyList<QualifiedName>(), QualifiedNames.find(source))
    }

    @Test
    fun wantsAReasonAfterTheMarker() {
        val source = "final class Example {\n  net.minecraft.world.entity.Display handle; // fqn:   \n}"
        assertEquals(listOf(QualifiedName(2, "net.minecraft.world.entity.Display")), QualifiedNames.find(source))
    }

    @Test
    fun ignoresCommentsAndJavadoc() {
        val source = """
            /**
             * Wraps a {@link java.util.List}, see {@link org.bukkit.Bukkit#getServer()}.
             */
            final class Example {
              // java.util.Arrays would do this too
              /* org.bukkit.Bukkit */ int value;
            }
        """.trimIndent()
        assertEquals(emptyList<QualifiedName>(), QualifiedNames.find(source))
    }

    @Test
    fun ignoresStringsCharactersAndTextBlocks() {
        val quotes = "\"\"\""
        val source = listOf(
            "final class Example {",
            "  final String name = \"java.util.List \\\" org.bukkit.Bukkit\";",
            "  final char quote = '\"';",
            "  final String next = \"me.example.Other\";",
            "  final String block = $quotes",
            "      java.util.Arrays \\$quotes still text",
            "      org.bukkit.Bukkit",
            "      $quotes;",
            "}",
        ).joinToString("\n")
        assertEquals(emptyList<QualifiedName>(), QualifiedNames.find(source))
    }

    @Test
    fun ignoresPackageAndImportDeclarations() {
        val source = """
            package me.example.tool;

            import java.util.List;
            import static org.junit.jupiter.api.Assertions.assertEquals;

            final class Example {}
        """.trimIndent()
        assertEquals(emptyList<QualifiedName>(), QualifiedNames.find(source))
    }

    @Test
    fun ignoresMemberChainsAndOtherPackageRoots() {
        val source = """
            final class Example {
              int a = this.me.Values.size;
              int b = config.net.Limit;
              int c = unknown.util.Type.VALUE;
            }
        """.trimIndent()
        assertEquals(emptyList<QualifiedName>(), QualifiedNames.find(source))
    }

    @Test
    fun keepsTheLineOfCodeAfterAMultilineComment() {
        val source = "/*\n java.util.List\n*/ java.util.Set<String> set;"
        assertEquals(listOf(QualifiedName(3, "java.util.Set")), QualifiedNames.find(source))
    }
}
