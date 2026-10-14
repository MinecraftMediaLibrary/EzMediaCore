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

  @Override
  public int getVideoWidth() {
    return this.videoWidth;
  }

  @Override
  public int getVideoHeight() {
    return this.videoHeight;
  }

  @Override
  public int getVideoBitrate() {
    return this.videoBitrate;
  }

  @Override
  public float getVideoFrameRate() {
    return this.videoFrameRate;
  }

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

  @Override
  public int hashCode() {
    return Objects.hash(this.videoWidth, this.videoHeight, this.videoBitrate, this.videoFrameRate);
  }

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
