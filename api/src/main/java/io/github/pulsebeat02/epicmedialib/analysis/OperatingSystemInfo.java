package io.github.pulsebeat02.epicmedialib.analysis;

import org.jetbrains.annotations.NotNull;

public interface OperatingSystemInfo {
  @NotNull
  String getOSName();

  @NotNull
  OSType getOSType();

  @NotNull
  String getLinuxDistribution();

  @NotNull
  String getVersion();
}
