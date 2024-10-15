package io.github.pulsebeat02.ezmediacore.pipeline;

public interface FramePipelineResult<I> {
  void executePipeline(final I input);
  void releasePipelines();
}
