package io.github.pulsebeat02.ezmediacore.player.input.implementation;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.player.input.Input;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class PathInput implements Input {

  private static final Input EMPTY_PATH;

  static {
    EMPTY_PATH = ofPath("");
  }

  private final Path path;

  PathInput(@NotNull final String path) {
    checkNotNull(path, "URL specified cannot be null!");
    try {
      this.path = Path.of(path);
    } catch (final InvalidPathException e) {
      throw new IllegalArgumentException("Invalid path %s!".formatted(path));
    }
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Input ofPath(@NotNull final String path) {
    return new PathInput(path);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Input ofPath(@NotNull final Path path) {
    return ofPath(path.toString());
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull Input ofPath(@NotNull final File file) {
    return ofPath(file.toString());
  }

  public static @NotNull Input emptyPath() {
    return EMPTY_PATH;
  }

  @Override
  public @NotNull String getInput() {
    return this.path.toString();
  }

  @Override
  public void setupInput() {
  }

  @Contract(pure = true)
  @Override
  public @NotNull String toString() {
    return "{path=%s}".formatted(this.path);
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof PathInput)) {
      return false;
    }
    return ((PathInput) obj).path.equals(this.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path);
  }
}
