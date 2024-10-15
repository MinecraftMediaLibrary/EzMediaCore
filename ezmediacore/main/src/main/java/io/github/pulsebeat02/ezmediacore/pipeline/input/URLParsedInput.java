package io.github.pulsebeat02.ezmediacore.pipeline.input;

import io.github.pulsebeat02.ezmediacore.pipeline.input.downloader.DownloadableInput;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.URLInputParser;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.strategy.DefaultAudioStrategy;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.strategy.DefaultVideoStrategy;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.strategy.FormatStrategy;

import java.util.concurrent.CompletableFuture;

public final class URLParsedInput implements DownloadableInput {

  private final FormatStrategy strategy;
  private final URLInputParser parser;
  private final boolean video;

  public URLParsedInput(final URLInputParser parser, final FormatStrategy strategy, final boolean video) {
    this.parser = parser;
    this.strategy = strategy;
    this.video = video;
  }

  public URLParsedInput(final URLInputParser parser, final boolean video) {
    this(parser, video ? new DefaultVideoStrategy() : new DefaultAudioStrategy(), video);
  }

  @Override
  public CompletableFuture<String> getMediaRepresentation() {
    final CompletableFuture<Input> future = this.parser.retrieveInput(this.strategy);
    return future.thenCompose(Input::getMediaRepresentation);
  }

  public URLInputParser getParser() {
    return this.parser;
  }

  public boolean isVideo() {
    return this.video;
  }
}
