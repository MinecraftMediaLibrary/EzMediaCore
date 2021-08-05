package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import io.github.pulsebeat02.ezmediacore.dimension.Dimensional;
import org.jetbrains.annotations.NotNull;

public interface VideoFormat extends Dimensional {

  int getFPS();

  @NotNull
  String getQualityLabel();

  @NotNull
  VideoQuality getQuality();
}
