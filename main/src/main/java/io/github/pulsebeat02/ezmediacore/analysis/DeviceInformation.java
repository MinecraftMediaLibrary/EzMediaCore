package io.github.pulsebeat02.ezmediacore.analysis;

import org.jetbrains.annotations.NotNull;

public class DeviceInformation {

  private final boolean bits64;
  private final OSType os;
  private final boolean arm;

  DeviceInformation(
      final boolean bits64,
      @NotNull final OSType os,
      final boolean arm) {
    this.bits64 = bits64;
    this.os = os;
    this.arm = arm;
  }

  public static @NotNull DeviceInformation ofDeviceInfo(final boolean bits64,
      @NotNull final OSType os, final boolean arm) {
    return new DeviceInformation(bits64, os, arm);
  }

  public boolean isBits64() {
    return this.bits64;
  }

  public @NotNull OSType getOs() {
    return this.os;
  }

  public boolean isArm() {
    return this.arm;
  }
}
