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
package me.brandonli.mcav.media.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import me.brandonli.mcav.MCAVException;
import org.junit.jupiter.api.Test;

final class PlayerExceptionTest {

  @Test
  void shouldKeepTheMessage() {
    final PlayerException exception = new PlayerException("Media video.mp4 could not be opened by VLC!");
    final String message = exception.getMessage();
    final Throwable cause = exception.getCause();
    assertEquals("Media video.mp4 could not be opened by VLC!", message);
    assertNull(cause);
  }

  @Test
  void shouldKeepTheMessageAndTheCause() {
    final IOException connectFailure = new IOException("connection refused!");
    final PlayerException exception = new PlayerException("The VNC server cannot be reached!", connectFailure);
    final String message = exception.getMessage();
    final Throwable cause = exception.getCause();
    assertEquals("The VNC server cannot be reached!", message);
    assertSame(connectFailure, cause);
  }

  @Test
  void shouldBeAnMCAVException() {
    final PlayerException exception = new PlayerException("failure!");
    assertInstanceOf(MCAVException.class, exception, "a player failure is recoverable, so it is no Error");
  }
}
