package io.github.pulsebeat02.ezmediacore.player.input.implementation;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.player.input.InputItem;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class WindowInput implements InputItem {

  private static final InputItem EMPTY_WINDOW;

  static {
    EMPTY_WINDOW = ofWindowName("");
  }

  private final String windowName;

  WindowInput(@NotNull final String windowName) {
    checkNotNull(windowName, "Window name specified cannot be null!");
    this.windowName = windowName;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull InputItem ofWindowName(@NotNull final String deviceName) {
    return new WindowInput(deviceName);
  }

  public static @NotNull InputItem emptyWindow() {
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
