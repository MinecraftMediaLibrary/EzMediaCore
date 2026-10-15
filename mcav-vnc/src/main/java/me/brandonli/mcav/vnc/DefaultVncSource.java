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

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The default {@link VncSource}, created by {@link VncSource#builder()}.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @since 1.0.0
 */
public final class DefaultVncSource implements VncSource {

  private final String host;
  private final int port;
  private final @Nullable String username;
  private final @Nullable String password;
  private final int width;
  private final int height;
  private final int targetFrameRate;

  DefaultVncSource(
    final String host,
    final int port,
    final @Nullable String username,
    final @Nullable String password,
    final int width,
    final int height,
    final int targetFrameRate
  ) {
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
    this.width = width;
    this.height = height;
    this.targetFrameRate = targetFrameRate;
  }

  /**
   * Returns the host name or address this source was built with, as given.
   *
   * @return the host
   * @since 1.0.0
   */
  @Override
  public String getHost() {
    return this.host;
  }

  /**
   * Returns the port of the server.
   *
   * @return the TCP port, from 1 to 65535
   * @since 1.0.0
   */
  @Override
  public int getPort() {
    return this.port;
  }

  /**
   * Returns the user name sent to servers that require one.
   *
   * @return the user name, or empty if none was set
   * @since 1.0.0
   */
  @Override
  public Optional<String> getUsername() {
    return Optional.ofNullable(this.username);
  }

  /**
   * Returns the password sent to servers that require one.
   *
   * @return the password, or empty if none was set
   * @since 1.0.0
   */
  @Override
  public Optional<String> getPassword() {
    return Optional.ofNullable(this.password);
  }

  /**
   * Returns the width the frames are scaled to.
   *
   * @return the width in pixels, or 0 to use each remote frame's width
   * @since 1.0.0
   */
  @Override
  public int getScreenWidth() {
    return this.width;
  }

  /**
   * Returns the height the frames are scaled to.
   *
   * @return the height in pixels, or 0 to use each remote frame's height
   * @since 1.0.0
   */
  @Override
  public int getScreenHeight() {
    return this.height;
  }

  /**
   * Returns the frame rate requested from the server.
   *
   * @return the positive requested frame rate in frames per second
   * @since 1.0.0
   */
  @Override
  public int getTargetFrameRate() {
    return this.targetFrameRate;
  }

  /**
   * Returns whether {@code other} is a default VNC source with the same address, credentials, frame size and
   * frame rate.
   *
   * @param other the object to compare with
   * @return whether all settings match
   * @since 1.0.0
   */
  @Override
  public boolean equals(final @Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof final DefaultVncSource source)) {
      return false;
    }

    return (
      this.host.equals(source.host) &&
      this.port == source.port &&
      Objects.equals(this.username, source.username) &&
      Objects.equals(this.password, source.password) &&
      this.width == source.width &&
      this.height == source.height &&
      this.targetFrameRate == source.targetFrameRate
    );
  }

  /**
   * Returns a hash code built from every setting.
   *
   * @return the hash code
   * @since 1.0.0
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.host, this.port, this.username, this.password, this.width, this.height, this.targetFrameRate);
  }

  /**
   * Returns a debugging representation with the address, frame size and frame rate; credentials are left out.
   *
   * @return the representation
   * @since 1.0.0
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("host", this.host)
      .add("port", this.port)
      .add("width", this.width)
      .add("height", this.height)
      .add("targetFrameRate", this.targetFrameRate)
      .toString();
  }
}
