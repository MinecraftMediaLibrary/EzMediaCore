package io.github.pulsebeat02.ezmediacore.pipeline.input.parser.strategy;

import io.github.pulsebeat02.ezmediacore.pipeline.input.Input;
import io.github.pulsebeat02.ezmediacore.pipeline.input.URLInput;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.extractor.Format;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.extractor.URLParseDump;

import java.util.List;

public final class DefaultAudioStrategy implements FormatStrategy {

  @Override
  public Input getNeededInput(final URLParseDump dump) {
    final List<Format> formats = dump.formats;
    for (final Format format : formats) {
      final String ext = format.audio_ext;
      if (!this.isAudioFormat(ext)) {
        continue;
      }
      final String url = format.url;
      return new URLInput(url);
    }
    return null;
  }

  private boolean isAudioFormat(final String ext) {
    return !ext.equals("none");
  }
}
