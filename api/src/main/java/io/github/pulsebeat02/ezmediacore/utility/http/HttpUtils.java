package io.github.pulsebeat02.ezmediacore.utility.http;

import org.jetbrains.annotations.NotNull;

public final class HttpUtils {

  private HttpUtils() {}

  public static boolean checkTreeAttack(@NotNull final String result) {
    return result.startsWith("..") || result.endsWith("..") || result.contains("../");
  }
}
