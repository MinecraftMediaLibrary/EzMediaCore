package io.github.pulsebeat02.deluxemediaplugin.json;

import com.google.gson.Gson;

public final class GsonProvider {

  private static final Gson GSON;

  static {
    GSON = new Gson();
  }

  public static Gson getGson() {
    return GSON;
  }
}
