/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
  public void setupInput() {}

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
