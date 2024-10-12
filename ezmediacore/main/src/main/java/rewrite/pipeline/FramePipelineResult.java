package rewrite.pipeline;

@FunctionalInterface
public interface FramePipelineResult<I> {
  void executePipeline(final I input);
}
