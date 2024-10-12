package rewrite.pipeline;

import rewrite.pipeline.outputs.FrameOutputSource;
import rewrite.pipeline.steps.FramePipelineStep;

import java.util.List;

public final class BasicPipelineResult implements FramePipelineResult {

  private final List<FramePipelineProcedure> steps;
  private final FrameOutputSource output;

  BasicPipelineResult(final List<FramePipelineProcedure> steps,
                      final FrameOutputSource output) {
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
    this.output.output(current);
  }
}
