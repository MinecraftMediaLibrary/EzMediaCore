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
package me.brandonli.mcav.json.ytdlp.format;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * One fragment of a segmented stream, as listed by yt-dlp.
 *
 * <p>Gson fills instances from the JSON yt-dlp prints. Values yt-dlp did not report are empty, zero or false; a
 * primitive zero or false does not tell a missing value from a reported one.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @since 1.0.0
 */
public final class Fragment {

  private final @Nullable String url;

  private final double duration;

  private Fragment() {
    this.url = null;
    this.duration = 0.0;
  }

  /**
   * Returns the URL of the fragment, or empty if yt-dlp did not report one.
   *
   * @return the URL of the fragment, or empty if yt-dlp did not report one
   * @since 1.0.0
   */
  public Optional<String> getUrl() {
    return Optional.ofNullable(this.url);
  }

  /**
   * Returns the length of the fragment in seconds.
   *
   * @return the length of the fragment in seconds
   * @since 1.0.0
   */
  public double getDuration() {
    return this.duration;
  }

  /**
   * Returns whether {@code other} is a fragment with the same values.
   *
   * @param other the object to compare with
   * @return whether every value matches
   * @since 1.0.0
   */
  @Override
  public boolean equals(final @Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof final Fragment fragment)) {
      return false;
    }

    return Objects.equals(this.url, fragment.url) && Double.compare(this.duration, fragment.duration) == 0;
  }

  /**
   * Returns a hash code built from every value.
   *
   * @return the hash code
   * @since 1.0.0
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.url, this.duration);
  }

  /**
   * Returns a debugging representation with every value.
   *
   * @return the representation
   * @since 1.0.0
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("url", this.url).add("duration", this.duration).toString();
  }
}
