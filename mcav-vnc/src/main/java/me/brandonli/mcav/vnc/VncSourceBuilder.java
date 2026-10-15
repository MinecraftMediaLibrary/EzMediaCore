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
package me.brandonli.mcav.vnc;

/**
 * Collects the settings of a {@link VncSource}. The host is required; the port defaults to
 * {@link VncSource#DEFAULT_PORT}, the frame size to the size of the remote screen, and the frame rate to
 * {@link VncSource#DEFAULT_FRAME_RATE}. Builders are mutable and not thread-safe; built sources keep their own settings.
 */
public interface VncSourceBuilder {
  /**
   * Sets the host name or address of the server.
   *
   * @param host the non-null, nonblank host name or address, retained without trimming
   * @return this builder
   * @throws NullPointerException     if {@code host} is null
   * @throws IllegalArgumentException if {@code host} is blank
   */
  VncSourceBuilder host(final String host);

  /**
   * Sets the port of the server.
   *
   * @param port the port from 1 to 65535
   * @return this builder
   * @throws IllegalArgumentException if {@code port} is outside 1 through 65535
   */
  VncSourceBuilder port(final int port);

  /**
   * Sets the user name sent to the server.
   *
   * @param username the non-null user name; an empty string makes the default player omit this credential
   * @return this builder
   * @throws NullPointerException if {@code username} is null
   */
  VncSourceBuilder username(final String username);

  /**
   * Sets the password sent to the server.
   *
   * @param password the non-null password; an empty string makes the default player omit this credential
   * @return this builder
   * @throws NullPointerException if {@code password} is null
   */
  VncSourceBuilder password(final String password);

  /**
   * Sets the width the frames are scaled to.
   *
   * @param width the width in pixels, or 0 to keep the width of the remote screen
   * @return this builder
   * @throws IllegalArgumentException if {@code width} is negative
   */
  VncSourceBuilder screenWidth(final int width);

  /**
   * Sets the height the frames are scaled to.
   *
   * @param height the height in pixels, or 0 to keep the height of the remote screen
   * @return this builder
   * @throws IllegalArgumentException if {@code height} is negative
   */
  VncSourceBuilder screenHeight(final int height);

  /**
   * Sets the frame rate requested from the server.
   *
   * @param targetFrameRate the strictly positive frame rate in frames per second
   * @return this builder
   * @throws IllegalArgumentException if {@code targetFrameRate} is zero or negative
   */
  VncSourceBuilder targetFrameRate(final int targetFrameRate);

  /**
   * Builds the source.
   *
   * @return a new immutable source; subsequent builder changes do not affect it
   * @throws IllegalStateException if no host was set
   */
  VncSource build();
}
