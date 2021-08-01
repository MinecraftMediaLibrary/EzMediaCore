package io.github.pulsebeat02.ezmediacore.vlc.os;

import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import org.jetbrains.annotations.NotNull;

public interface HostOperator {

  @NotNull
  OSType getOperatingSystem();
}
