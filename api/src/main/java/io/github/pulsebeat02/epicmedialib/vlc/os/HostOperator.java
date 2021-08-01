package io.github.pulsebeat02.epicmedialib.vlc.os;

import io.github.pulsebeat02.epicmedialib.analysis.OSType;
import org.jetbrains.annotations.NotNull;

public interface HostOperator {

  @NotNull
  OSType getOperatingSystem();
}
