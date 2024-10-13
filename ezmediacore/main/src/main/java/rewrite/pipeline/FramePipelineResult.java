package rewrite.pipeline;

public interface FramePipelineResult<I> {
  void executePipeline(final I input);
  void releasePipelines();
}
