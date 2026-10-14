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
package me.brandonli.mcav.media.player.metadata;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The default {@link OriginalVideoMetadata}.
 */
public final class DefaultOriginalVideoMetadata implements OriginalVideoMetadata {

  private final int videoWidth;
  private final int videoHeight;
  private final int videoBitrate;
  private final float videoFrameRate;

  DefaultOriginalVideoMetadata(final int videoWidth, final int videoHeight, final int videoBitrate, final float videoFrameRate) {
    this.videoWidth = videoWidth;
    this.videoHeight = videoHeight;
    this.videoBitrate = videoBitrate;
    this.videoFrameRate = videoFrameRate;
  }

  /**
   * Returns the width of the decoded frames.
   *
   * @return the width in pixels, or {@link #UNKNOWN} in {@link #EMPTY}
   */
  @Override
  public int getVideoWidth() {
    return this.videoWidth;
  }

  /**
   * Returns the height of the decoded frames.
   *
   * @return the height in pixels, or {@link #UNKNOWN} in {@link #EMPTY}
   */
  @Override
  public int getVideoHeight() {
    return this.videoHeight;
  }

  /**
   * Returns the bitrate of the video stream.
   *
   * @return the bitrate in bits per second, or {@link #UNKNOWN}
   */
  @Override
  public int getVideoBitrate() {
    return this.videoBitrate;
  }

  /**
   * Returns the frame rate of the video stream.
   *
   * @return the frame rate in frames per second, or {@link #UNKNOWN}
   */
  @Override
  public float getVideoFrameRate() {
    return this.videoFrameRate;
  }

  /**
   * Returns whether {@code other} is a video metadata with the same size, bitrate and frame rate.
   *
   * @param other the object to compare with
   * @return whether both match
   */
  @Override
  public boolean equals(final @Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof final DefaultOriginalVideoMetadata metadata)) {
      return false;
    }

    final int frameRateComparison = Float.compare(this.videoFrameRate, metadata.videoFrameRate);
    return (
      this.videoWidth == metadata.videoWidth &&
      this.videoHeight == metadata.videoHeight &&
      this.videoBitrate == metadata.videoBitrate &&
      frameRateComparison == 0
    );
  }

  /**
   * Returns a hash code built from the size, bitrate and frame rate.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.videoWidth, this.videoHeight, this.videoBitrate, this.videoFrameRate);
  }

  /**
   * Returns a debugging representation with the size, bitrate and frame rate.
   *
   * @return the representation
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("videoWidth", this.videoWidth)
      .add("videoHeight", this.videoHeight)
      .add("videoBitrate", this.videoBitrate)
      .add("videoFrameRate", this.videoFrameRate)
      .toString();
  }
}
