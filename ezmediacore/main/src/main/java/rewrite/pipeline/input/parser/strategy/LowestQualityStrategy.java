package rewrite.pipeline.input.parser.strategy;

import rewrite.pipeline.input.Input;
import rewrite.pipeline.input.parser.extractor.URLParseDump;

import java.util.Comparator;

public final class LowestQualityStrategy implements FormatStrategy {

  @Override
  public Input getNeededInput(final URLParseDump dump) {
    


    return dump.formats.stream().min(Comparator.comparingInt(f -> f.format.height)).map(f -> new Input(f.url, f.format_id)).orElse(null);
  }
}
