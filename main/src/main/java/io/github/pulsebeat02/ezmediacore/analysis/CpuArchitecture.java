package io.github.pulsebeat02.ezmediacore.analysis;

import org.jetbrains.annotations.NotNull;

public record CpuArchitecture(String architecture, boolean bits64) implements CpuInfo {

  public CpuArchitecture(@NotNull final String architecture, final boolean bits64) {
    this.architecture = architecture;
    this.bits64 = bits64;
  }

  @Override
  public String toString() {
    return "[arch=%s,64bits=%s]".formatted(this.architecture, this.bits64);
  }
}
