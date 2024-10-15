package io.github.pulsebeat02.ezmediacore.pipeline.input.parser.strategy;

import io.github.pulsebeat02.ezmediacore.pipeline.input.Input;
import io.github.pulsebeat02.ezmediacore.pipeline.input.URLInput;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.extractor.Format;
import io.github.pulsebeat02.ezmediacore.pipeline.input.parser.extractor.URLParseDump;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class LowestQualityVideoStrategy implements FormatStrategy {

  @Override
  public Input getNeededInput(final URLParseDump dump) {
    final List<Format> formats = dump.formats;
    final Optional<Format> optionalFormat = formats.stream().filter(Predicate.not(format -> this.isVideoFormat(format.ext))).min(Comparator.comparingDouble(format -> format.quality));
    return optionalFormat.map(format -> new URLInput(format.url)).orElse(null);
  }

  private boolean isVideoFormat(final String ext) {
    return !ext.equals("none");
  }
}