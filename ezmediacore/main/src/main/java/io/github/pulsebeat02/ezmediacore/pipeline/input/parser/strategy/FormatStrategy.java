package io.github.pulsebeat02.ezmediacore.pipeline.input.parser.strategy;

import io.github.pulsebeat02.ezmediacore.pipeline.input.Input;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.extractor.URLParseDump;

@FunctionalInterface
public interface FormatStrategy {
  Input getNeededInput(final URLParseDump dump);
}
