package rewrite.pipeline.steps;

@FunctionalInterface
public interface FramePipelineStep<I, O> {
  O process(final I input);
}
