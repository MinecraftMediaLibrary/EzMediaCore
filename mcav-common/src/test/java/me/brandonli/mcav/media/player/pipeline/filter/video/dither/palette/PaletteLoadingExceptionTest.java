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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.gson.JsonSyntaxException;
import me.brandonli.mcav.MCAVException;
import org.junit.jupiter.api.Test;

final class PaletteLoadingExceptionTest {

  @Test
  void shouldKeepTheMessage() {
    final PaletteLoadingException exception = new PaletteLoadingException("Map palette resource is empty!");
    final String message = exception.getMessage();
    final Throwable cause = exception.getCause();
    assertEquals("Map palette resource is empty!", message);
    assertNull(cause);
  }

  @Test
  void shouldKeepTheMessageAndTheCause() {
    final JsonSyntaxException parseFailure = new JsonSyntaxException("Unterminated array!");
    final PaletteLoadingException exception = new PaletteLoadingException("Map palette resource is malformed!", parseFailure);
    final String message = exception.getMessage();
    final Throwable cause = exception.getCause();
    assertEquals("Map palette resource is malformed!", message);
    assertSame(parseFailure, cause);
  }

  @Test
  void shouldBeAnMCAVException() {
    final PaletteLoadingException exception = new PaletteLoadingException("Failure!");
    assertInstanceOf(MCAVException.class, exception, "a broken bundled palette is a broken library state, not an Error");
  }
}
