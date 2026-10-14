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
package me.brandonli.mcav.media.player.pipeline.filter.video.dither.palette;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.code_intelligence.jazzer.junit.FuzzTest;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Tag;

@Tag("fuzz")
final class MapPaletteLoaderFuzzTest {

  private static final int ALPHA_SHIFT = 24;
  private static final int OPAQUE_ALPHA = 0xFF;

  @FuzzTest(maxDuration = "30s")
  void shouldParseAPaletteOrRefuseTheText(final byte[] input) {
    final String text = new String(input, StandardCharsets.UTF_8);
    final Reader reader = new StringReader(text);
    final int[] colors;
    try {
      colors = MapPaletteLoader.parseColors(reader);
    } catch (final PaletteLoadingException exception) {
      return;
    }

    for (final int color : colors) {
      final int alpha = color >>> ALPHA_SHIFT;
      assertEquals(OPAQUE_ALPHA, alpha);
    }
  }
}
