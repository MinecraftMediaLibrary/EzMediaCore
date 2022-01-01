package io.github.pulsebeat02.ezmediacore.player.input.implementation;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.player.input.Input;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class DeviceInput implements Input {

  private static final Input EMPTY_DEVICE;

  static {
    EMPTY_DEVICE = ofDeviceName("");
  }

  private final String deviceName;

  DeviceInput(@NotNull final String deviceName) {
    checkNotNull(deviceName, "Device name specified cannot be null!");
    this.deviceName = deviceName;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Input ofDeviceName(@NotNull final String deviceName) {
    return new DeviceInput(deviceName);
  }

  public static @NotNull Input emptyDevice() {
    return EMPTY_DEVICE;
  }

  @Override
  public @NotNull String getInput() {
    return this.deviceName;
  }

  @Override
  public void setupInput() {
  }

  @Contract(pure = true)
  @Override
  public @NotNull String toString() {
    return "{device=%s}".formatted(this.deviceName);
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof DeviceInput)) {
      return false;
    }
    return ((DeviceInput) obj).deviceName.equals(this.deviceName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.deviceName);
  }
}
