package rewrite.pipeline;

import rewrite.pipeline.steps.FramePipelineStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class FramePipelineStart implements FramePipelineProcedure {

  private final List<FramePipelineStep<Object, Object>> steps;

  public FramePipelineStart() {
    this.steps = new ArrayList<>();
  }

  @Override
  public List<FramePipelineStep<Object, Object>> getSteps() {
    return this.steps;
  }

  @Override
  public void addSteps(final Collection<FramePipelineStep<Object, Object>> steps) {
    this.steps.addAll(steps);
  }

  @Override
  public void addStep(final FramePipelineStep<Object, Object> step) {
    this.steps.add(step);
  }

  @Override
  public void removeStep(final FramePipelineStep<Object, Object> step) {
    this.steps.remove(step);
  }
}
