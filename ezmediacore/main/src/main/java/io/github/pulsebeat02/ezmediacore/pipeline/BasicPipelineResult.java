package io.github.pulsebeat02.ezmediacore.pipeline;

import io.github.pulsebeat02.ezmediacore.pipeline.output.FrameOutputSource;
import io.github.pulsebeat02.ezmediacore.pipeline.steps.FramePipelineStep;

import java.util.List;

public final class BasicPipelineResult implements FramePipelineResult {

  private final List<FramePipelineProcedure> steps;
  private final List<FrameOutputSource> output;

  BasicPipelineResult(final List<FramePipelineProcedure> steps,
                      final List<FrameOutputSource> output) {
    this.steps = steps;
    this.output = output;
  }

  @Override
  public void executePipeline(final Object input) {
    Object current = input;
    for (final FramePipelineProcedure procedure : this.steps) {
      final List<FramePipelineStep<Object, Object>> steps = procedure.getSteps();
      for (final FramePipelineStep<Object, Object> step : steps) {
        current = step.process(current);
      }
    }
    for (final FrameOutputSource output : this.output) {
      output.output(current);
    }
  }

  @Override
  public void releasePipelines() {
    for (final FrameOutputSource output : this.output) {
      output.release();
    }
  }
}
