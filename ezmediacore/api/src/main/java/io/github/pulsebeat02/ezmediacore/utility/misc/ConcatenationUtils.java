package io.github.pulsebeat02.ezmediacore.utility.misc;

import java.util.Map;

public final class ConcatenationUtils {

  private ConcatenationUtils() {}

  public static String mapOutputString(final Map<String, String> configuration, final String raw) {
    final StringBuilder builder = new StringBuilder("[");
    for (final Map.Entry<String, String> entry : configuration.entrySet()) {
      builder.append(entry.getKey()).append("=").append(entry.getValue()).append(":");
    }
    builder.replace(builder.length() - 1, builder.length(), "]");
    builder.append(raw);
    return builder.toString();
  }

}
