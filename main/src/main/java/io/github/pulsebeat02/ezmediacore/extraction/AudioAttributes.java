/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.extraction;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public final class AudioAttributes implements AudioConfiguration {

  private final String codec;
  private final int start;
  private final int bitrate;
  private final int channels;
  private final int samplingRate;
  private final int volume;

  public AudioAttributes(
      @NotNull final String codec,
      final int start,
      final int bitrate,
      final int channels,
      final int samplingRate,
      final int volume) {
    Preconditions.checkArgument(start >= 0, "Invalid Start Time! (%d)".formatted(start));
    Preconditions.checkArgument(bitrate > 0, "Invalid Bitrate! (%d)".formatted(bitrate));
    Preconditions.checkArgument(channels > 0, "Invalid Channels! (%d)".formatted(channels));
    Preconditions.checkArgument(samplingRate > 0,
        "Invalid Sampling Rate! (%d)".formatted(samplingRate));
    Preconditions.checkArgument(volume >= 0, "Invalid Volume! (%d)".formatted(volume));
    this.start = start;
    this.codec = codec;
    this.bitrate = bitrate;
    this.channels = channels;
    this.samplingRate = samplingRate;
    this.volume = volume;
  }

  public AudioAttributes(
      @NotNull final String codec,
      final int bitrate,
      final int channels,
      final int samplingRate,
      final int volume) {
    this(codec, 0, bitrate, channels, samplingRate, volume);
  }

  @Override
  public int getBitrate() {
    return this.bitrate;
  }

  @Override
  public int getChannels() {
    return this.channels;
  }

  @Override
  public int getSamplingRate() {
    return this.samplingRate;
  }

  @Override
  public @NotNull String getCodec() {
    return this.codec;
  }

  @Override
  public int getVolume() {
    return this.volume;
  }

  @Override
  public int getStartTime() {
    return this.start;
  }
}
