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
import com.google.gson.annotations.SerializedName;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Download hints yt-dlp reports for a stream.
 *
 * <p>Gson fills instances from the JSON yt-dlp prints. Values yt-dlp did not report are empty, zero or false; a
 * primitive zero or false does not tell a missing value from a reported one.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @since 1.0.0
 */
public final class DownloaderOptions {

  @SerializedName("http_chunk_size")
  private final int httpChunkSize;

  private DownloaderOptions() {
    this.httpChunkSize = 0;
  }

  /**
   * Returns the size in bytes of the chunks yt-dlp requests the stream in, which avoids the throttling of some
   * sites, or zero if the stream is requested at once.
   *
   * @return the size in bytes of the chunks yt-dlp requests the stream in, which avoids the throttling of some
   * sites, or zero if the stream is requested at once
   * @since 1.0.0
   */
  public int getHttpChunkSize() {
    return this.httpChunkSize;
  }

  /**
   * Returns whether {@code other} is a set of download hints with the same values.
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

    if (!(other instanceof final DownloaderOptions downloaderOptions)) {
      return false;
    }

    return this.httpChunkSize == downloaderOptions.httpChunkSize;
  }

  /**
   * Returns a hash code built from the HTTP chunk size.
   *
   * @return the hash code
   * @since 1.0.0
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.httpChunkSize);
  }

  /**
   * Returns a debugging representation with the HTTP chunk size.
   *
   * @return the representation
   * @since 1.0.0
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("httpChunkSize", this.httpChunkSize).toString();
  }
}
