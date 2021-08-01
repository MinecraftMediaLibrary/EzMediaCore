package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import io.github.pulsebeat02.ezmediacore.utility.Dimension;
import org.jetbrains.annotations.NotNull;

public interface VideoFormat extends Dimension {

  int getFPS();

  @NotNull
  String getQualityLabel();

  @NotNull
  VideoQuality getQuality();
}
