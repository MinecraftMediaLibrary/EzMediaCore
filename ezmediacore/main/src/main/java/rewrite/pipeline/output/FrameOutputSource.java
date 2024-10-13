package rewrite.pipeline.output;

@FunctionalInterface
public interface FrameOutputSource<I> {
  void output(final I input);
  default void release() {}
}
