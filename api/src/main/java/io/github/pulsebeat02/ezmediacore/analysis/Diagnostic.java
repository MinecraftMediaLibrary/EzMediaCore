package io.github.pulsebeat02.ezmediacore.analysis;

import java.util.List;
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
  List<Mixer> getSound();
}
