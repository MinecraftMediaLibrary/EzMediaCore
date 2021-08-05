package io.github.pulsebeat02.ezmediacore.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import io.github.pulsebeat02.ezmediacore.json.adapter.PathAdapter;
import io.github.pulsebeat02.ezmediacore.json.adapter.UUIDAdapter;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public final class GsonProvider {

  private static final Gson SIMPLE;
  private static final Gson PRETTY;

  static {
    final Map<Class<?>, TypeAdapter<?>> adapters =
        Map.of(
            Path.class, new PathAdapter(),
            UUID.class, new UUIDAdapter());

    final GsonBuilder builder = new GsonBuilder();
    adapters.forEach(builder::registerTypeAdapter);

    SIMPLE = builder.create();
    PRETTY = builder.setPrettyPrinting().create();
  }

  public static Gson getSimple() {
    return SIMPLE;
  }

  public static Gson getPretty() {
    return PRETTY;
  }
}
