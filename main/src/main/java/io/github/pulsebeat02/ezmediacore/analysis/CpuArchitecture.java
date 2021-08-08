package io.github.pulsebeat02.ezmediacore.analysis;

import org.jetbrains.annotations.NotNull;

public class CpuArchitecture implements CpuInfo {

  private final String architecture;
  private final boolean bits64;

  public CpuArchitecture(@NotNull final String architecture, final boolean bits64) {
    this.architecture = architecture;
    this.bits64 = bits64;
  }

  @Override
  public String toString() {
    return "[arch=%s,64bits=%s]".formatted(this.architecture, this.bits64);
  }

  @Override
  public @NotNull String getArchitecture() {
    return this.architecture;
  }

  @Override
  public boolean isBits64() {
    return this.bits64;
  }
}
