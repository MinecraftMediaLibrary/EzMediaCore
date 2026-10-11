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

class VariableNamesTest {

    @Test
    fun acceptsDescriptiveNamesTypeParametersAndTheUnnamedVariable() {
        val source = """
            package example;
            final class Pairs<K, V> {
              private final int count;
              <T> T first(final List<T> values, final int index) {
                for (final T value : values) {
                  return value;
                }
                values.forEach(_ -> { });
                return null;
              }
            }
        """.trimIndent()
        assertEquals(emptyList<UndescriptiveName>(), VariableNames.find(source))
    }

    @Test
    fun reportsEveryKindOfDeclarationNamedByOneLetter() {
        val source = """
            package example;
            final class Shapes {
              private int w;
              record Point(int x, int height) {}
              void draw(final int y) throws Exception {
                for (int i = 0; i < 3; i++) {}
                for (final Object o : List.of()) {}
                try (final Stream s = open()) {
                } catch (final IOException e) {
                }
                Runnable r = () -> {};
                Function<Integer, Integer> f = n -> n;
                if (this instanceof Object p) {}
              }
            }
        """.trimIndent()
        val kinds = VariableNames.find(source).map { "${it.name}: ${it.kind}" }
        assertEquals(
            listOf(
                "w: field",
                "x: record component",
                "y: parameter",
                "i: loop variable",
                "o: loop variable",
                "s: resource",
                "e: catch parameter",
                "r: local variable",
                "f: local variable",
                "n: lambda parameter",
                "p: pattern variable",
            ),
            kinds,
        )
    }

    @Test
    fun reportsTheShortFormsTheRulesListButNotWordsThatStartWithThem() {
        val source = """
            final class Buffers {
              void fill(final byte[] buf, final int idx) {
                final int bufferSize = buf.length;
                final StringBuilder sb = new StringBuilder();
                final String string = sb.toString();
              }
            }
        """.trimIndent()
        assertEquals(listOf("buf", "idx", "sb"), VariableNames.find(source).map { it.name })
    }

    @Test
    fun namesTheMemberAsThePackageTheTypesAndTheName() {
        val source = """
            package me.example;
            public final class Frame {
              public record Leaf(int x, int size) {}
              void run() {
                new Object() {
                  int q;
                };
              }
            }
        """.trimIndent()
        val found = VariableNames.find(source)
        assertEquals(listOf("me.example.Frame.Leaf#x", "me.example.Frame.<anonymous>#q"), found.map { it.member })
        assertEquals(listOf(3L, 6L), found.map { it.line })
    }
}
