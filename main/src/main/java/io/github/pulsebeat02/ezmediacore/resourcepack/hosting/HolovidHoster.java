package io.github.pulsebeat02.ezmediacore.resourcepack.hosting;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.json.GsonProvider;
import org.jcodec.codecs.mjpeg.tools.AssertionException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HolovidHoster implements HolovidSolution {

  private static final String HOLOVID_LINK;

  static {
    HOLOVID_LINK = "https://holovid.glare.dev/resourcepack/download?videoUrl=";
  }

  @Override
  public @NotNull String createUrl(@NotNull final String input) {
    try (final InputStream in = new URL(HOLOVID_LINK + input).openStream()) {
      return GsonProvider.getSimple()
          .fromJson(
              new String(in.readAllBytes(), StandardCharsets.UTF_8),
              HolovidResourcepackResult.class)
          .getUrl();
    } catch (IOException e) {
      Logger.info(
          "Holovid hosting site https://holovid.glare.dev is down! Contact PulseBeat_02 for information!");
      e.printStackTrace();
    }
    throw new AssertionException("Holovid website is down!");
  }
}
