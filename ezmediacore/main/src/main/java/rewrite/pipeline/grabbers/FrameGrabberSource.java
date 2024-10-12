package rewrite.pipeline.grabbers;

@FunctionalInterface
public interface FrameGrabberSource<O> {
  O grabOutputFrame();
}
