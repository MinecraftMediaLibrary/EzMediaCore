package io.github.pulsebeat02.ezmediacore.player.input.implementation;

import io.github.pulsebeat02.ezmediacore.player.input.Input;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class DesktopInput implements Input {

  private static final Input DEFAULT_DESKTOP;

  static {
    DEFAULT_DESKTOP = ofDesktop();
  }

  private DesktopInput() {}

  public static Input defaultDesktop() {
    return DEFAULT_DESKTOP;
  }

  public static DesktopInput ofDesktop() {
    return new DesktopInput();
  }

  @Override
  public @NotNull String getInput() {
    return "desktop";
  }

  @Override
  public void setupInput() {}

  @Contract(pure = true)
  @Override
  public @NotNull String toString() {
    return "{desktop=%s}".formatted(this.getInput());
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof DesktopInput)) {
      return false;
    }
    return ((DesktopInput) obj).getInput().equals(this.getInput());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getInput());
  }
}
