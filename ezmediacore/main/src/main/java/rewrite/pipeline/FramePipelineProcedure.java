package rewrite.pipeline;

import rewrite.pipeline.steps.FramePipelineStep;

import java.util.Collection;
import java.util.List;

public interface FramePipelineProcedure {
  List<FramePipelineStep<Object, Object>> getSteps();

  void addSteps(final Collection<FramePipelineStep<Object, Object>> steps);

  void addStep(final FramePipelineStep<Object, Object> step);

  void removeStep(final FramePipelineStep<Object, Object> step);
}
