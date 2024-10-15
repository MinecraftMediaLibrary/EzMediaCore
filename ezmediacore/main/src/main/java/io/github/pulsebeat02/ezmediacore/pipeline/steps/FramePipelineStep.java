package io.github.pulsebeat02.ezmediacore.pipeline.steps;

@FunctionalInterface
public interface FramePipelineStep<I, O> {
  O process(final I input);
}
