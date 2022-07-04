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

import static com.google.common.base.Preconditions.checkArgument;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class AudioAttributes implements AudioConfiguration {

  public static final AudioAttributes OGG_CONFIGURATION;

  static {
    OGG_CONFIGURATION = AudioAttributes.ofAudioAttributes("libvorbis", 0, 160000, 2, 44100, 50);
  }

  private final String codec;
  private final int start;
  private final int bitrate;
  private final int channels;
  private final int samplingRate;
  private final int volume;

  AudioAttributes(
      @NotNull final String codec,
      final int start,
      final int bitrate,
      final int channels,
      final int samplingRate,
      final int volume) {
    checkArgument(start >= 0, "Invalid Start Time!");
    checkArgument(bitrate > 0, "Invalid Bitrate!");
    checkArgument(channels > 0, "Invalid Channels!");
    checkArgument(samplingRate > 0, "Invalid Sampling Rate!");
    checkArgument(volume >= 0, "Invalid Volume!");
    this.start = start;
    this.codec = codec;
    this.bitrate = bitrate;
    this.channels = channels;
    this.samplingRate = samplingRate;
    this.volume = volume;
  }

  AudioAttributes(
      @NotNull final String codec,
      final int bitrate,
      final int channels,
      final int samplingRate,
      final int volume) {
    this(codec, 0, bitrate, channels, samplingRate, volume);
  }

  @Contract("_, _, _, _, _, _ -> new")
  public static @NotNull AudioAttributes ofAudioAttributes(
      @NotNull final String codec,
      final int start,
      final int bitrate,
      final int channels,
      final int samplingRate,
      final int volume) {
    return new AudioAttributes(codec, start, bitrate, channels, samplingRate, volume);
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
