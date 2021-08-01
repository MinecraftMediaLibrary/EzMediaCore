package io.github.pulsebeat02.ezmediacore.utility;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ResourceUtils {

  private ResourceUtils() {}

  @NotNull
  public static String getFileContents(@NotNull final String name) throws IOException {
    final ClassLoader loader = ResourceUtils.class.getClassLoader();
    final InputStream input = loader.getResourceAsStream(name);
    if (input == null) {
      throw new NullPointerException(String.format("File not Found! %s", name));
    } else {
      return IOUtils.toString(input, StandardCharsets.UTF_8.name());
    }
  }

  @Nullable
  public static InputStream accessResourceFileJar(@NotNull final String resource) {
    InputStream input =
        ResourceUtils.class.getResourceAsStream(String.format("/resources/%s", resource));
    if (input == null) {
      input = ResourceUtils.class.getClassLoader().getResourceAsStream(resource);
    }
    return input;
  }
}
