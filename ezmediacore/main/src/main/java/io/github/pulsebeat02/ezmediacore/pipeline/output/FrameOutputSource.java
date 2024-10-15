package io.github.pulsebeat02.ezmediacore.pipeline.output;

@FunctionalInterface
public interface FrameOutputSource<I> {
  void output(final I input);
  default void release() {}
}
