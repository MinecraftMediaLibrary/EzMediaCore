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

import java.util.Optional;
import me.brandonli.mcav.media.source.DynamicSource;

/**
 * A VNC server to stream: its address, optional credentials, the size frames are scaled to, and the frame rate
 * requested from the server. The default builder produces immutable values that can be shared across threads.
 * Creating a source performs no network I/O and does not create a connection.
 *
 * <pre><code>
 *   final VncSourceBuilder builder = VncSource.builder();
 *   builder.host("localhost");
 *   builder.port(5900);
 *   builder.password("secret");
 *   builder.screenWidth(1280);
 *   builder.screenHeight(720);
 *   builder.targetFrameRate(30);
 *   final VncSource source = builder.build();
 * </code></pre>
 */
public interface VncSource extends DynamicSource {
  /**
   * The port VNC servers listen on unless configured otherwise.
   */
  int DEFAULT_PORT = 5900;

  /**
   * The frame rate requested when none is set.
   */
  int DEFAULT_FRAME_RATE = 30;

  /**
   * Creates a builder for a source.
   *
   * @return the builder
   */
  static VncSourceBuilder builder() {
    return new DefaultVncSourceBuilder();
  }

  /**
   * Returns the host name or address of the server.
   *
   * @return the host
   */
  String getHost();

  /**
   * Returns the port of the server.
   *
   * @return the TCP port, from 1 to 65535
   */
  int getPort();

  /**
   * Returns the user name for servers that require one.
   *
   * @return the user name, or empty if none is sent
   */
  Optional<String> getUsername();

  /**
   * Returns the password for servers that require one.
   *
   * @return the password, or empty if none is sent
   */
  Optional<String> getPassword();

  /**
   * Returns the width the frames are scaled to.
   *
   * @return the nonnegative configured width in pixels, or 0 to use each remote frame's width
   */
  int getScreenWidth();

  /**
   * Returns the height the frames are scaled to.
   *
   * @return the nonnegative configured height in pixels, or 0 to use each remote frame's height
   */
  int getScreenHeight();

  /**
   * Returns the frame rate requested from the server. The server sends fewer frames when nothing changes.
   *
   * @return the positive requested frame rate in frames per second; this is not a delivery guarantee
   */
  int getTargetFrameRate();

  /**
   * Builds a diagnostic resource identifier from the host and port, without credentials.
   *
   * @return {@code vnc://} followed by the host verbatim, a colon and the decimal port; this does not
   *         escape or bracket the host and is not a general-purpose URI serializer
   */
  @Override
  default String getResource() {
    final String host = this.getHost();
    final int port = this.getPort();
    return "vnc://%s:%s".formatted(host, port);
  }

  /**
   * Returns the source kind.
   *
   * @return {@code vnc}
   */
  @Override
  default String getName() {
    return "vnc";
  }
}
