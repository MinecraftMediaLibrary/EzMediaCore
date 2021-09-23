package io.github.pulsebeat02.ezmediacore.jlibdl;

import java.io.IOException;
import org.jetbrains.annotations.Nullable;

public final class JLibDL {

  public @Nullable YoutubeDLRequest request(@Nullable final String url)
      throws IOException, InterruptedException {
    return YoutubeDLRequest.request(url);
  }
}
