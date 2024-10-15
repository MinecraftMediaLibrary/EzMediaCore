package rewrite.pipeline.steps;

import rewrite.dither.algorithm.random.RandomDither;
import rewrite.dither.load.ColorPalette;
import rewrite.pipeline.frame.DitheredFramePacket;
import rewrite.pipeline.frame.DitheredPacket;
import rewrite.pipeline.frame.FramePacket;

public final class RandomDitherPipelineStep implements FramePipelineStep<FramePacket, DitheredPacket> {

  private final RandomDither dither;

  public RandomDitherPipelineStep(final ColorPalette palette, final int weight) {
    this.dither = new RandomDither(palette, weight, false);
  }

  @Override
  public DitheredPacket process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    final byte[] dithered = this.dither.standardMinecraftDither(rgb, width);
    return DitheredFramePacket.create(input, dithered);
  }
}
