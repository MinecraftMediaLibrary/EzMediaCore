package rewrite.pipeline.outputs;

@FunctionalInterface
public interface FrameOutputSource<I> {
  void output(final I input);
}
