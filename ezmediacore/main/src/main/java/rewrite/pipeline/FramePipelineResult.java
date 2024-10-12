package rewrite.pipeline;

@FunctionalInterface
public interface FramePipelineResult {
  void executePipeline(final Object input);
}
