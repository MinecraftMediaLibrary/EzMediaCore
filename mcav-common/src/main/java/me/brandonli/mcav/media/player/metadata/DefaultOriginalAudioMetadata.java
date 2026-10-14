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
 * The default {@link OriginalAudioMetadata}.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @since 1.0.0
 */
public final class DefaultOriginalAudioMetadata implements OriginalAudioMetadata {

  private final String codec;
  private final int audioBitrate;
  private final int audioSampleRate;
  private final int audioChannels;
  private final int samplingFormat;

  DefaultOriginalAudioMetadata(
    final String codec,
    final int audioBitrate,
    final int audioSampleRate,
    final int audioChannels,
    final int samplingFormat
  ) {
    this.codec = codec;
    this.audioBitrate = audioBitrate;
    this.audioSampleRate = audioSampleRate;
    this.audioChannels = audioChannels;
    this.samplingFormat = samplingFormat;
  }

  /**
   * Returns the audio codec name that the decoder reported when the stream opened.
   *
   * @return the codec name
   * @since 1.0.0
   */
  @Override
  public String getAudioCodec() {
    return this.codec;
  }

  /**
   * Returns the FFmpeg sample format identifier that the decoder reported when the stream opened.
   *
   * @return the sample format, or {@link #UNKNOWN}
   * @since 1.0.0
   */
  @Override
  public int getSamplingFormat() {
    return this.samplingFormat;
  }

  /**
   * Returns the audio bitrate that the decoder reported when the stream opened.
   *
   * @return the bitrate in bits per second, or {@link #UNKNOWN}
   * @since 1.0.0
   */
  @Override
  public int getAudioBitrate() {
    return this.audioBitrate;
  }

  /**
   * Returns the audio sample rate that the decoder reported when the stream opened.
   *
   * @return the sample rate in hertz
   * @since 1.0.0
   */
  @Override
  public int getAudioSampleRate() {
    return this.audioSampleRate;
  }

  /**
   * Returns the audio channel count that the decoder reported when the stream opened.
   *
   * @return the channel count
   * @since 1.0.0
   */
  @Override
  public int getAudioChannels() {
    return this.audioChannels;
  }

  /**
   * Returns whether {@code other} is an audio metadata with the same codec, bitrate, sample rate, channel count and sampling format.
   *
   * @param other the object to compare with
   * @return whether both match
   * @since 1.0.0
   */
  @Override
  public boolean equals(final @Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof final DefaultOriginalAudioMetadata metadata)) {
      return false;
    }

    return (
      this.codec.equals(metadata.codec) &&
      this.audioBitrate == metadata.audioBitrate &&
      this.audioSampleRate == metadata.audioSampleRate &&
      this.audioChannels == metadata.audioChannels &&
      this.samplingFormat == metadata.samplingFormat
    );
  }

  /**
   * Returns a hash code built from the codec, bitrate, sample rate, channel count and sampling format.
   *
   * @return the hash code
   * @since 1.0.0
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.codec, this.audioBitrate, this.audioSampleRate, this.audioChannels, this.samplingFormat);
  }

  /**
   * Returns a debugging representation with the codec, bitrate, sample rate, channel count and sampling format.
   *
   * @return the representation
   * @since 1.0.0
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("codec", this.codec)
      .add("audioBitrate", this.audioBitrate)
      .add("audioSampleRate", this.audioSampleRate)
      .add("audioChannels", this.audioChannels)
      .add("samplingFormat", this.samplingFormat)
      .toString();
  }
}
