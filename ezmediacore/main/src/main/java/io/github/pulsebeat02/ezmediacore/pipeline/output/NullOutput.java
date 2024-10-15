package io.github.pulsebeat02.ezmediacore.pipeline.output;

public final class NullOutput<T> implements FrameOutputSource<T> {

  @Override
  public void output(final T input) {
  }
}
