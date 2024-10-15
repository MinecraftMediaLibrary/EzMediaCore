package io.github.pulsebeat02.ezmediacore.pipeline.input;

import java.util.concurrent.CompletableFuture;

public final class URLInput implements Input {

  private final String url;

  public URLInput(final String url) {
    this.url = url;
  }

  @Override
  public CompletableFuture<String> getMediaRepresentation() {
    return CompletableFuture.completedFuture(this.url);
  }

  public String getUrl() {
    return this.url;
  }
}
