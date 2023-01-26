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
import io.github.pulsebeat02.ezmediacore.utility.graphics.JNAWindow;
import io.github.pulsebeat02.ezmediacore.utility.graphics.WindowUtils;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class WindowInput implements Input {

  private static final Input EMPTY_WINDOW;

  static {
    EMPTY_WINDOW = ofWindowName("");
  }

  private final String windowName;

  WindowInput(@NotNull final String windowName) {
    checkNotNull(windowName, "Window name specified cannot be null!");
    this.windowName = this.checkValidWindow(windowName);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Input ofWindowName(@NotNull final String deviceName) {
    return new WindowInput(deviceName);
  }

  public static @NotNull Input emptyWindow() {
    return EMPTY_WINDOW;
  }

  private @NotNull String checkValidWindow(@NotNull final String name) {
    final String upper = name.toUpperCase(Locale.ROOT);
    final List<JNAWindow> windows = WindowUtils.getAllWindows();
    for (final JNAWindow window : windows) {
      final String original = window.getTitle();
      final String check = original.toUpperCase(Locale.ROOT);
      if (check.contains(upper)) {
        return original;
      }
    }
    throw new IllegalArgumentException(
        "Invalid window title! Not displayable or simply doesn't exist!");
  }

  @Override
  public @NotNull String getInput() {
    return this.windowName;
  }

  @Override
  public void setupInput() {}

  @Contract(pure = true)
  @Override
  public @NotNull String toString() {
    return "{window=%s}".formatted(this.windowName);
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof WindowInput)) {
      return false;
    }
    return ((WindowInput) obj).windowName.equals(this.windowName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.windowName);
  }
}
