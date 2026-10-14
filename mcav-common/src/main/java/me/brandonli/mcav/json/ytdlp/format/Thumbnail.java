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
 * One thumbnail of a video, as listed by yt-dlp.
 *
 * <p>Gson fills instances from the JSON yt-dlp prints. Values yt-dlp did not report are empty, zero or false; a
 * primitive zero or false does not tell a missing value from a reported one.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @since 1.0.0
 */
public final class Thumbnail {

  private final @Nullable String url;

  private final int preference;

  private final @Nullable String id;

  private final int height;

  private final int width;

  private final @Nullable String resolution;

  private Thumbnail() {
    this.url = null;
    this.preference = 0;
    this.id = null;
    this.height = 0;
    this.width = 0;
    this.resolution = null;
  }

  /**
   * Returns the URL of the image, or empty if yt-dlp did not report it.
   *
   * @return the URL of the image, or empty if yt-dlp did not report it
   * @since 1.0.0
   */
  public Optional<String> getUrl() {
    return Optional.ofNullable(this.url);
  }

  /**
   * Returns how strongly yt-dlp prefers the thumbnail over the others, where higher is better.
   *
   * @return how strongly yt-dlp prefers the thumbnail over the others, where higher is better
   * @since 1.0.0
   */
  public int getPreference() {
    return this.preference;
  }

  /**
   * Returns the identifier of the thumbnail among the thumbnails of the video, or empty if yt-dlp did not report
   * it.
   *
   * @return the identifier of the thumbnail among the thumbnails of the video, or empty if yt-dlp did not report it
   * @since 1.0.0
   */
  public Optional<String> getId() {
    return Optional.ofNullable(this.id);
  }

  /**
   * Returns the height of the image in pixels, or zero if unknown.
   *
   * @return the height of the image in pixels, or zero if unknown
   * @since 1.0.0
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Returns the width of the image in pixels, or zero if unknown.
   *
   * @return the width of the image in pixels, or zero if unknown
   * @since 1.0.0
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Returns the resolution of the image as text, such as {@code 1920x1080}, or empty if unknown.
   *
   * @return the resolution of the image as text, such as {@code 1920x1080}, or empty if unknown
   * @since 1.0.0
   */
  public Optional<String> getResolution() {
    return Optional.ofNullable(this.resolution);
  }

  /**
   * Returns whether {@code other} is a thumbnail with the same values.
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

    if (!(other instanceof final Thumbnail thumbnail)) {
      return false;
    }

    return (
      Objects.equals(this.url, thumbnail.url) &&
      this.preference == thumbnail.preference &&
      Objects.equals(this.id, thumbnail.id) &&
      this.height == thumbnail.height &&
      this.width == thumbnail.width &&
      Objects.equals(this.resolution, thumbnail.resolution)
    );
  }

  /**
   * Returns a hash code built from every value.
   *
   * @return the hash code
   * @since 1.0.0
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.url, this.preference, this.id, this.height, this.width, this.resolution);
  }

  /**
   * Returns a debugging representation with every value.
   *
   * @return the representation
   * @since 1.0.0
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("url", this.url)
      .add("preference", this.preference)
      .add("id", this.id)
      .add("height", this.height)
      .add("width", this.width)
      .add("resolution", this.resolution)
      .toString();
  }
}
