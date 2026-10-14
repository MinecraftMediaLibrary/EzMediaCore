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
package me.brandonli.mcav.media.source.ffmpeg;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The default {@link FfmpegDirectSource}.
 *
 * <p>This class is immutable and thread-safe.
 */
public final class DefaultFfmpegDirectSource implements FfmpegDirectSource {

  private final String mrl;
  private final String format;

  DefaultFfmpegDirectSource(final String mrl, final String format) {
    this.mrl = mrl;
    this.format = format;
  }

  /**
   * Returns the FFmpeg input format.
   *
   * @return the format name
   */
  @Override
  public String getFormat() {
    return this.format;
  }

  /**
   * Returns the input FFmpeg opens.
   *
   * @return the media resource locator
   */
  @Override
  public String getMrl() {
    return this.mrl;
  }

  /**
   * Returns whether {@code other} is a direct FFmpeg source with the same resource and the format.
   *
   * @param other the object to compare with
   * @return whether both match
   */
  @Override
  public boolean equals(final @Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof final DefaultFfmpegDirectSource source)) {
      return false;
    }

    return this.mrl.equals(source.mrl) && this.format.equals(source.format);
  }

  /**
   * Returns a hash code built from the resource and the format.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.mrl, this.format);
  }

  /**
   * Returns a debugging representation with the resource and the format.
   *
   * @return the representation
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("mrl", this.mrl).add("format", this.format).toString();
  }
}
