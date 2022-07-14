package io.github.pulsebeat02.ezmediacore.utility.io;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;

import static java.util.Objects.requireNonNull;

public final class ResourceUtils {

  private ResourceUtils() {}

  public static @NotNull InputStreamReader getResourceAsInputStream(
      @NotNull final String resource) {
    return new InputStreamReader(
        requireNonNull(MediaLibraryCore.class.getResourceAsStream(resource)));
  }
}
