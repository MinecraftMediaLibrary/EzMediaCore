package rewrite.pipeline.input;

import rewrite.pipeline.input.parser.URLInputParser;
import rewrite.pipeline.input.parser.strategy.DefaultAudioStrategy;
import rewrite.pipeline.input.parser.strategy.DefaultVideoStrategy;
import rewrite.pipeline.input.parser.strategy.FormatStrategy;

import java.util.concurrent.CompletableFuture;

public final class URLParsedInput implements Input {

  private final URLInputParser parser;
  private final boolean video;

  public URLParsedInput(final URLInputParser parser, final boolean video) {
    this.parser = parser;
    this.video = video;
  }

  @Override
  public CompletableFuture<String> getMediaRepresentation() {
    final FormatStrategy defaultAudio = new DefaultAudioStrategy();
    final FormatStrategy defaultVideo = new DefaultVideoStrategy();
    final CompletableFuture<Input> future =
            this.video ? this.parser.retrieveVideoInput(defaultVideo) : this.parser.retrieveAudioInput(defaultAudio);
    return future.thenCompose(Input::getMediaRepresentation);
  }

  public URLInputParser getParser() {
    return this.parser;
  }

  public boolean isVideo() {
    return this.video;
  }
}
