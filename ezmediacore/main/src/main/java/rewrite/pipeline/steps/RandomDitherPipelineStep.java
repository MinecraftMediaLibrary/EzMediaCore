package rewrite.pipeline.steps;

import rewrite.dither.algorithm.random.RandomDither;
import rewrite.pipeline.frame.FramePacket;

public final class RandomDitherPipelineStep implements FramePipelineStep<FramePacket, FramePacket> {

  private final RandomDither dither;

  public RandomDitherPipelineStep(final int weight, final boolean useNative) {
    this.dither = new RandomDither(weight, useNative);
  }

  public RandomDitherPipelineStep(final int weight) {
    this.dither = new RandomDither(weight);
  }

  @Override
  public FramePacket process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    this.dither.dither(rgb, width);
    return input;
  }
}
