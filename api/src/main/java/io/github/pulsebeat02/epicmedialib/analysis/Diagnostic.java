package io.github.pulsebeat02.epicmedialib.analysis;

import java.util.Collection;
import javax.sound.sampled.Mixer;
import org.jetbrains.annotations.NotNull;

public interface Diagnostic {
  void debugInformation();

  @NotNull
  String getFFmpegUrl();

  @NotNull
  String getVlcUrl();

  @NotNull
  OperatingSystemInfo getSystem();

  @NotNull
  CpuInfo getCpu();

  @NotNull
  Collection<Mixer> getSound();
}
