package io.github.pulsebeat02.epicmedialib.extraction;

import org.jetbrains.annotations.NotNull;

public interface AudioConfiguration {

  int getBitrate();

  void setBitrate(final int bitrate);

  int getChannels();

  void setChannels(final int channels);

  int getSamplingRate();

  void setSamplingRate(final int samplingRate);

  String getCodec();

  void setCodec(@NotNull final String codec);

  int getVolume();

  void setVolume(final int volume);

  int getStartTime();

  void setStartTime(final int time);
}
