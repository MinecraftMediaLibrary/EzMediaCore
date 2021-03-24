/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/2/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.extractor;

/**
 * A class full of audio properties to specify the properties of an audio file. It is used
 * eventually in JAVE2 while audio conversion is taking place. Strongly recommended to research each
 * of these properties first or else you might make someone deaf!
 */
public class ExtractionSetting {

  private final String codec;
  private final String format;
  private int bitrate;
  private int channels;
  private int samplingRate;
  private int volume;

  /**
   * Instantiates a new ExtractionSetting.
   *
   * @param bitrate the bitrate
   * @param channels the channels
   * @param samplingRate the sampling rate
   * @param volume the volume
   */
  public ExtractionSetting(
      final int bitrate, final int channels, final int samplingRate, final int volume) {
    codec = "libvorbis";
    format = "ogg";
    this.bitrate = bitrate;
    this.channels = channels;
    this.samplingRate = samplingRate;
    this.volume = volume;
  }

  /**
   * Gets codec.
   *
   * @return the codec
   */
  public String getCodec() {
    return codec;
  }

  /**
   * Gets format.
   *
   * @return the format
   */
  public String getFormat() {
    return format;
  }

  /**
   * Gets bitrate.
   *
   * @return the bitrate
   */
  public int getBitrate() {
    return bitrate;
  }

  /**
   * Sets bitrate.
   *
   * @param bitrate the bitrate
   */
  public void setBitrate(final int bitrate) {
    this.bitrate = bitrate;
  }

  /**
   * Gets channels.
   *
   * @return the channels
   */
  public int getChannels() {
    return channels;
  }

  /**
   * Sets channels.
   *
   * @param channels the channels
   */
  public void setChannels(final int channels) {
    this.channels = channels;
  }

  /**
   * Gets sampling rate.
   *
   * @return the sampling rate
   */
  public int getSamplingRate() {
    return samplingRate;
  }

  /**
   * Sets sampling rate.
   *
   * @param samplingRate the sampling rate
   */
  public void setSamplingRate(final int samplingRate) {
    this.samplingRate = samplingRate;
  }

  /**
   * Gets volume.
   *
   * @return the volume
   */
  public int getVolume() {
    return volume;
  }

  /**
   * Sets volume.
   *
   * @param volume the volume
   */
  public void setVolume(final int volume) {
    this.volume = volume;
  }

  /** The type Builder. */
  public static class Builder {

    private int bitrate = 160000;
    private int channels = 2;
    private int samplingRate = 44100;
    private int volume = 48;

    /**
     * Sets bitrate.
     *
     * @param bitrate the bitrate
     * @return the bitrate
     */
    public Builder setBitrate(final int bitrate) {
      this.bitrate = bitrate;
      return this;
    }

    /**
     * Sets channels.
     *
     * @param channels the channels
     * @return the channels
     */
    public Builder setChannels(final int channels) {
      this.channels = channels;
      return this;
    }

    /**
     * Sets sampling rate.
     *
     * @param samplingRate the sampling rate
     * @return the sampling rate
     */
    public Builder setSamplingRate(final int samplingRate) {
      this.samplingRate = samplingRate;
      return this;
    }

    /**
     * Sets volume.
     *
     * @param volume the volume
     * @return the volume
     */
    public Builder setVolume(final int volume) {
      this.volume = volume;
      return this;
    }

    /**
     * Creates ExtractionSetting from Builder
     *
     * @return the extraction setting
     */
    public ExtractionSetting createExtractionSetting() {
      return new ExtractionSetting(bitrate, channels, samplingRate, volume);
    }
  }
}
