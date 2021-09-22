package io.github.pulsebeat02.ezmediacore.youtubedl;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public final class JLibDL {

  public @NotNull YoutubeDLRequest request(@NotNull final String url)
      throws IOException, InterruptedException {
    return YoutubeDLRequest.request(url);
  }
}
