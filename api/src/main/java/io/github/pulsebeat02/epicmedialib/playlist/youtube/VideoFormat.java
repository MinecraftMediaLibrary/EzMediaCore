package io.github.pulsebeat02.epicmedialib.playlist.youtube;

import io.github.pulsebeat02.epicmedialib.utility.Dimension;
import org.jetbrains.annotations.NotNull;

public interface VideoFormat extends Dimension {

  int getFPS();

  @NotNull
  String getQualityLabel();

  @NotNull
  VideoQuality getQuality();
}
