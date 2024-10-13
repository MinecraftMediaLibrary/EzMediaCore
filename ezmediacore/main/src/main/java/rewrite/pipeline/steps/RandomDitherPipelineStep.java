package rewrite.pipeline.steps;

import rewrite.dither.algorithm.random.RandomDither;
import rewrite.dither.load.ColorPalette;
import rewrite.pipeline.frame.FramePacket;

public final class RandomDitherPipelineStep implements FramePipelineStep<FramePacket, FramePacket> {

  private final RandomDither dither;

  public RandomDitherPipelineStep(final ColorPalette palette, final int weight) {
    this.dither = new RandomDither(palette, weight, false);
  }

  @Override
  public FramePacket process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    this.dither.dither(rgb, width);
    return input;
  }
}
