/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/22/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.extractor;

public class ExtractionSetting {

  private final String codec;
  private final String format;
  private int bitrate;
  private int channels;
  private int samplingRate;
  private int volume;

  public ExtractionSetting(int bitrate, int channels, int samplingRate, int volume) {
    this.codec = "libvorbis";
    this.format = "ogg";
    this.bitrate = bitrate;
    this.channels = channels;
    this.samplingRate = samplingRate;
    this.volume = volume;
  }

  public String getCodec() {
    return codec;
  }

  public String getFormat() {
    return format;
  }

  public int getBitrate() {
    return bitrate;
  }

  public void setBitrate(final int bitrate) {
    this.bitrate = bitrate;
  }

  public int getChannels() {
    return channels;
  }

  public void setChannels(final int channels) {
    this.channels = channels;
  }

  public int getSamplingRate() {
    return samplingRate;
  }

  public void setSamplingRate(final int samplingRate) {
    this.samplingRate = samplingRate;
  }

  public int getVolume() {
    return volume;
  }

  public void setVolume(final int volume) {
    this.volume = volume;
  }

  public static class Builder {
    private int bitrate = 160000;
    private int channels = 2;
    private int samplingRate = 44100;
    private int volume = 48;

    public Builder setBitrate(final int bitrate) {
      this.bitrate = bitrate;
      return this;
    }

    public Builder setChannels(final int channels) {
      this.channels = channels;
      return this;
    }

    public Builder setSamplingRate(final int samplingRate) {
      this.samplingRate = samplingRate;
      return this;
    }

    public Builder setVolume(final int volume) {
      this.volume = volume;
      return this;
    }

    public ExtractionSetting createExtractionSetting() {
      return new ExtractionSetting(bitrate, channels, samplingRate, volume);
    }
  }
}
