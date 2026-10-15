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
package me.brandonli.mcav.bukkit.media.mcv2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ResourceLock(Resources.LOCALE)
final class Mcv2PackLocaleTest {

  private static final String ROOT_PACK_FILE_NAME = "root.zip";
  private static final String LOCALIZED_PACK_FILE_NAME = "localized.zip";

  private static final String SHADER_LIBRARY_EXTENSION = ".glsl";
  private static final String VERTEX_SHADER_EXTENSION = ".vsh";
  private static final String FRAGMENT_SHADER_EXTENSION = ".fsh";

  @TempDir
  private Path directory;

  @ParameterizedTest
  @ValueSource(strings = { "ar-EG", "fa-IR", "bn-BD" })
  void shouldGenerateShaderNumbersIndependentOfTheServerLocale(final String languageTag) throws IOException {
    final Locale previous = Locale.getDefault(Locale.Category.FORMAT);
    final Mcv2Configuration configuration = Mcv2ConfigurationTest.complete().video(320, 180).pageSlots(2).streamId(9).build();
    try {
      Locale.setDefault(Locale.Category.FORMAT, Locale.ROOT);
      final Map<String, String> expected = this.shaders(configuration, ROOT_PACK_FILE_NAME);
      Locale.setDefault(Locale.Category.FORMAT, Locale.forLanguageTag(languageTag));
      final Map<String, String> actual = this.shaders(configuration, LOCALIZED_PACK_FILE_NAME);
      final boolean empty = expected.isEmpty();
      assertFalse(empty);

      final Set<String> expectedNames = expected.keySet();
      final Set<String> actualNames = actual.keySet();
      assertEquals(expectedNames, actualNames);

      for (final Map.Entry<String, String> shader : expected.entrySet()) {
        final String expectedShader = shader.getValue();
        final @Nullable String actualShader = actual.get(shader.getKey());
        assertEquals(expectedShader, actualShader, shader.getKey() + " must keep locale-independent GLSL numbers");
      }
    } finally {
      Locale.setDefault(Locale.Category.FORMAT, previous);
    }
  }

  private Map<String, String> shaders(final Mcv2Configuration configuration, final String name) throws IOException {
    final Path zip = this.directory.resolve(name);
    Mcv2Pack.write(configuration, true, zip);
    final Map<String, String> shaders = new HashMap<>();
    try (final ZipFile archive = new ZipFile(zip.toFile())) {
      for (final ZipEntry entry : archive.stream().toList()) {
        if (
          entry.getName().endsWith(SHADER_LIBRARY_EXTENSION) ||
          entry.getName().endsWith(VERTEX_SHADER_EXTENSION) ||
          entry.getName().endsWith(FRAGMENT_SHADER_EXTENSION)
        ) {
          try (final InputStream input = archive.getInputStream(entry)) {
            shaders.put(entry.getName(), new String(input.readAllBytes(), StandardCharsets.UTF_8));
          }
        }
      }
    }

    return shaders;
  }
}
