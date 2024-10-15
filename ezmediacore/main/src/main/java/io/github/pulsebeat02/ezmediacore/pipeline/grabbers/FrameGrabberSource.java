package io.github.pulsebeat02.ezmediacore.pipeline.grabbers;

@FunctionalInterface
public interface FrameGrabberSource<O> {
  O grabOutputFrame();
}
