package io.github.pulsebeat02.ezmediacore.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonProvider {

  private static final Gson SIMPLE;
  private static final Gson PRETTY;

  static {
    SIMPLE = new Gson();
    PRETTY = new GsonBuilder().setPrettyPrinting().create();
  }

  public static Gson getSimple() {
    return SIMPLE;
  }

  public static Gson getPretty() {
    return PRETTY;
  }
}
