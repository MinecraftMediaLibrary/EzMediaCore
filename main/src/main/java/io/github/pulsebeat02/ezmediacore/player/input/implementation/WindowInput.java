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

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Input ofWindowName(@NotNull final String deviceName) {
    return new WindowInput(deviceName);
  }

  public static @NotNull Input emptyWindow() {
    return EMPTY_WINDOW;
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
