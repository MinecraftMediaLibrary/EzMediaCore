package rewrite.pipeline.input.parser.strategy;

import rewrite.pipeline.input.Input;
import rewrite.pipeline.input.URLInput;
import rewrite.pipeline.input.parser.extractor.Format;
import rewrite.pipeline.input.parser.extractor.URLParseDump;

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
