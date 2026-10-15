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
    fun shouldReportAQualifiedNameInCode() {
        val source = """
            package me.example;

            final class Example {
              void run() {
                java.util.Arrays.fill(new int[1], 0);
              }
            }
        """.trimIndent()
        val found: List<QualifiedName> = QualifiedNames.find(source)
        assertEquals(listOf(QualifiedName(5, "java.util.Arrays")), found)
    }

    @Test
    fun shouldReportEveryOccurrenceAndStopAtTheOuterType() {
        val source = """
            final class Example {
              final java.util.Map.Entry<String, java.util.List<String>> entry = null;
            }
        """.trimIndent()
        val found: List<QualifiedName> = QualifiedNames.find(source)
        assertEquals(listOf(QualifiedName(2, "java.util.Map"), QualifiedName(2, "java.util.List")), found)
    }

    @Test
    fun shouldAcceptAClashMarkedWithItsReason() {
        val source = """
            import org.bukkit.entity.Display;

            final class Example {
              net.minecraft.world.entity.Display handle; // fqn: Display is imported as org.bukkit.entity.Display
            }
        """.trimIndent()
        val found: List<QualifiedName> = QualifiedNames.find(source)
        assertEquals(emptyList<QualifiedName>(), found)
    }

    @Test
    fun shouldWantAReasonAfterTheMarker() {
        val source = "final class Example {\n  net.minecraft.world.entity.Display handle; // fqn:   \n}"
        val found: List<QualifiedName> = QualifiedNames.find(source)
        assertEquals(listOf(QualifiedName(2, "net.minecraft.world.entity.Display")), found)
    }

    @Test
    fun shouldIgnoreCommentsAndJavadoc() {
        val source = """
            /**
             * Wraps a {@link java.util.List}, see {@link org.bukkit.Bukkit#getServer()}.
             */
            final class Example {
              // java.util.Arrays would do this too
              /* org.bukkit.Bukkit */ int value;
            }
        """.trimIndent()
        val found: List<QualifiedName> = QualifiedNames.find(source)
        assertEquals(emptyList<QualifiedName>(), found)
    }

    @Test
    fun shouldIgnoreStringsCharactersAndTextBlocks() {
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
        val found: List<QualifiedName> = QualifiedNames.find(source)
        assertEquals(emptyList<QualifiedName>(), found)
    }

    @Test
    fun shouldIgnorePackageAndImportDeclarations() {
        val source = """
            package me.example.tool;

            import java.util.List;
            import static org.junit.jupiter.api.Assertions.assertEquals;

            final class Example {}
        """.trimIndent()
        val found: List<QualifiedName> = QualifiedNames.find(source)
        assertEquals(emptyList<QualifiedName>(), found)
    }

    @Test
    fun shouldIgnoreMemberChainsAndOtherPackageRoots() {
        val source = """
            final class Example {
              int a = this.me.Values.size;
              int b = config.net.Limit;
              int c = unknown.util.Type.VALUE;
            }
        """.trimIndent()
        val found: List<QualifiedName> = QualifiedNames.find(source)
        assertEquals(emptyList<QualifiedName>(), found)
    }

    @Test
    fun shouldKeepTheLineOfCodeAfterAMultilineComment() {
        val source = "/*\n java.util.List\n*/ java.util.Set<String> set;"
        val found: List<QualifiedName> = QualifiedNames.find(source)
        assertEquals(listOf(QualifiedName(3, "java.util.Set")), found)
    }
}
