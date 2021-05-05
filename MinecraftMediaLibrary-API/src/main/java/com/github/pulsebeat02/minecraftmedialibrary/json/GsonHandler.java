package com.github.pulsebeat02.minecraftmedialibrary.json;

import com.google.gson.Gson;

/** A simple class containing the Gson instance used throughout the library. */
public class GsonHandler {

  private static final Gson GSON;

  static {
    GSON = new Gson();
  }

  /**
   * Gets the Gson instance.
   *
   * @return the Gson instance
   */
  public static Gson getGson() {
    return GSON;
  }
}
