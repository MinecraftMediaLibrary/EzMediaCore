package rewrite.pipeline.input.parser.strategy;

import rewrite.pipeline.input.Input;
import rewrite.pipeline.input.parser.extractor.URLParseDump;

@FunctionalInterface
public interface FormatStrategy {
  Input getNeededInput(final URLParseDump dump);
}
