package io.github.pulsebeat02.ezmediacore.extraction;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public class AudioAttributes implements AudioConfiguration {

  private String codec;
  private int start;
  private int bitrate;
  private int channels;
  private int samplingRate;
  private int volume;

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
    Preconditions.checkArgument(
        samplingRate > 0, "Invalid Sampling Rate! (%d)".formatted(samplingRate));
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
  public void setBitrate(final int bitrate) {
    this.bitrate = bitrate;
  }

  @Override
  public int getChannels() {
    return this.channels;
  }

  @Override
  public void setChannels(final int channels) {
    this.channels = channels;
  }

  @Override
  public int getSamplingRate() {
    return this.samplingRate;
  }

  @Override
  public void setSamplingRate(final int samplingRate) {
    this.samplingRate = samplingRate;
  }

  @Override
  public String getCodec() {
    return this.codec;
  }

  @Override
  public void setCodec(@NotNull final String codec) {
    this.codec = codec;
  }

  @Override
  public int getVolume() {
    return this.volume;
  }

  @Override
  public void setVolume(final int volume) {
    this.volume = volume;
  }

  @Override
  public int getStartTime() {
    return this.start;
  }

  @Override
  public void setStartTime(final int time) {
    this.start = time;
  }
}
