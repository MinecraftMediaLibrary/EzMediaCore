package io.github.pulsebeat02.ezmediacore.player;

import java.io.File;
import java.nio.file.Path;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class MrlConfiguration {

  private static final MrlConfiguration EMPTY_MRL;

  static {
    EMPTY_MRL = ofMrl("");
  }

  private final String mrl;

  MrlConfiguration(@NotNull final String mrl) {
    this.mrl = mrl;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MrlConfiguration ofMrl(@NotNull final String mrl) {
    return new MrlConfiguration(mrl);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MrlConfiguration ofMrl(@NotNull final Path path) {
    return ofMrl(path.toString());
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MrlConfiguration ofMrl(@NotNull final File path) {
    return ofMrl(path.getPath());
  }

  public static @NotNull MrlConfiguration emptyMrl() {
    return EMPTY_MRL;
  }

  public @NotNull String getMrl() {
    return this.mrl;
  }
}
