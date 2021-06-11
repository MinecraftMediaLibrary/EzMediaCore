/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.extractor;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.minecraftmedialibrary.json.GsonHandler;
import org.jetbrains.annotations.NotNull;

/**
 * A class full of audio properties to specify the properties of an audio file. It is used
 * eventually in JAVE2 while audio conversion is taking place. Strongly recommended to research each
 * of these properties first or else you might make someone deaf!
 */
public class ExtractionSetting implements ExtractionConfiguration {

  private String codec;
  private int start;
  private int bitrate;
  private int channels;
  private int samplingRate;
  private int volume;

  /**
   * Instantiates a new ExtractionSetting.
   *
   * @param codec the codec
   * @param start the start time in seconds
   * @param bitrate the bitrate in bits
   * @param channels the channels
   * @param samplingRate the sampling rate in bits
   * @param volume the volume
   */
  public ExtractionSetting(
      @NotNull final String codec,
      final int start,
      final int bitrate,
      final int channels,
      final int samplingRate,
      final int volume) {
    Preconditions.checkArgument(start >= 0, String.format("Invalid Start Time! (%d)", start));
    Preconditions.checkArgument(bitrate > 0, String.format("Invalid Bitrate! (%d)", bitrate));
    Preconditions.checkArgument(channels > 0, String.format("Invalid Channels! (%d)", channels));
    Preconditions.checkArgument(
        samplingRate > 0, String.format("Invalid Sampling Rate! (%d)", samplingRate));
    Preconditions.checkArgument(volume >= 0, String.format("Invalid Volume! (%d)", volume));
    this.start = start;
    this.codec = codec;
    this.bitrate = bitrate;
    this.channels = channels;
    this.samplingRate = samplingRate;
    this.volume = volume;
  }

  /**
   * Instantiates a new ExtractionSetting.
   *
   * @param bitrate the bitrate
   * @param channels the channels
   * @param samplingRate the sampling rate
   * @param volume the volume
   * @param codec the codec
   */
  public ExtractionSetting(
      @NotNull final String codec,
      final int bitrate,
      final int channels,
      final int samplingRate,
      final int volume) {
    this(codec, 0, bitrate, channels, samplingRate, volume);
  }

  /**
   * Returns a new builder class to use.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof ExtractionSetting)) {
      return false;
    }
    final ExtractionConfiguration setting = (ExtractionConfiguration) obj;
    return setting.getBitrate() == bitrate
        && setting.getChannels() == channels
        && setting.getSamplingRate() == samplingRate
        && setting.getVolume() == volume;
  }

  @Override
  public String toString() {
    return GsonHandler.getGson().toJson(this);
  }

  @Override
  public int getBitrate() {
    return bitrate;
  }

  @Override
  public void setBitrate(final int bitrate) {
    this.bitrate = bitrate;
  }

  @Override
  public int getChannels() {
    return channels;
  }

  @Override
  public void setChannels(final int channels) {
    this.channels = channels;
  }

  @Override
  public int getSamplingRate() {
    return samplingRate;
  }

  @Override
  public void setSamplingRate(final int samplingRate) {
    this.samplingRate = samplingRate;
  }

  @Override
  public String getCodec() {
    return codec;
  }

  @Override
  public void setCodec(@NotNull final String codec) {
    this.codec = codec;
  }

  @Override
  public int getVolume() {
    return volume;
  }

  @Override
  public void setVolume(final int volume) {
    this.volume = volume;
  }

  @Override
  public int getStartTime() {
    return start;
  }

  @Override
  public void setStartTime(final int time) {
    start = time;
  }

  /** The type Builder. */
  public static class Builder {

    private String codec = "libvorbis";
    private int start = 0;
    private int bitrate = 160000;
    private int channels = 2;
    private int samplingRate = 44100;
    private int volume = 48;

    private Builder() {}

    public Builder bitrate(final int bitrate) {
      this.bitrate = bitrate;
      return this;
    }

    public Builder channels(final int channels) {
      this.channels = channels;
      return this;
    }

    public Builder samplingRate(final int samplingRate) {
      this.samplingRate = samplingRate;
      return this;
    }

    public Builder volume(final int volume) {
      this.volume = volume;
      return this;
    }

    public Builder codec(@NotNull final String codec) {
      this.codec = codec;
      return this;
    }

    public Builder startTime(final int start) {
      this.start = start;
      return this;
    }

    public ExtractionConfiguration build() {
      return new ExtractionSetting(codec, start, bitrate, channels, samplingRate, volume);
    }
  }
}
