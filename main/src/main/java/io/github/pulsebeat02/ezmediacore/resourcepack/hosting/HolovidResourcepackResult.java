package io.github.pulsebeat02.ezmediacore.resourcepack.hosting;

import org.jetbrains.annotations.NotNull;

public final class HolovidResourcepackResult implements ResourcepackResult {

  private final String url;
  private final String hash;

  public HolovidResourcepackResult(final String url, final String hash) {
    this.url = url;
    this.hash = hash;
  }

  @Override
  public @NotNull String getUrl() {
    return url;
  }

  @Override
  public @NotNull String getHash() {
    return hash;
  }
}
