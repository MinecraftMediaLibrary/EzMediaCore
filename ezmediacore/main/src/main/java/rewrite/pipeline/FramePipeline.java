package rewrite.pipeline;

import rewrite.pipeline.outputs.FrameOutputSource;
import rewrite.pipeline.steps.FramePipelineStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class FramePipeline<I, O> {

  private final List<FramePipelineProcedure> steps;
  private final Class<I> source;

  FramePipeline(final Class<I> source,
                final List<FramePipelineProcedure> steps) {
    this.steps = steps;
    this.steps.add(new FramePipelineStart());
    this.source = source;
  }

  public static <T> FramePipeline<T, T> source(final Class<T> clazz) {
    return new FramePipeline<>(clazz, new ArrayList<>());
  }

  @SafeVarargs
  public final <T> FramePipeline<I, T> thenPipe(final FramePipelineStep<O, T>... steps) {
    final List<FramePipelineStep<Object, Object>> list = Arrays.stream(steps)
            .map(step -> (FramePipelineStep<Object, Object>) step)
            .collect(Collectors.toList());
    final FramePipelineProcedure latest = this.steps.getLast();
    latest.addSteps(list);
    return new FramePipeline<>(this.source, this.steps);
  }

  public BasicPipelineResult thenFinally(final FrameOutputSource<O>... output) {
    final List<FrameOutputSource> list = Arrays.asList(output);
    return new BasicPipelineResult(this.steps, list);
  }
}
