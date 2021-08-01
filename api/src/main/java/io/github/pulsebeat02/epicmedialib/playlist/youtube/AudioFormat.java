package io.github.pulsebeat02.epicmedialib.playlist.youtube;

import org.jetbrains.annotations.NotNull;

public interface AudioFormat {

  int getBitrate();

  int getSamplingRate();

  @NotNull
  AudioQuality getAudioQuality();
}
