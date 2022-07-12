package io.github.pulsebeat02.ezmediacore.locale;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.pulsebeat02.ezmediacore.json.GsonProvider;
import io.github.pulsebeat02.ezmediacore.utility.io.ResourceUtils;
import io.github.pulsebeat02.nativelibraryloader.os.Platform;
import io.github.pulsebeat02.nativelibraryloader.strategy.implementation.UrlResourceNativeLibrary;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class MessageLoader {

  private MessageLoader() {}
  private static final Map<String, String> INTERNAL_LOCALE;

  static {
    final Gson gson = GsonProvider.getSimple();
    try (final Reader reader =
        ResourceUtils.getResourceAsInputStream("/emc-json/locale/english.json")) {
      final TypeToken<Map<String, String>> token = new TypeToken<>() {};
      INTERNAL_LOCALE = gson.fromJson(reader, token.getType());
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static @NotNull String key(@NotNull final String key) {
    return requireNonNull(INTERNAL_LOCALE.get(key), "Missing translation key %s".formatted(key));
  }
}
