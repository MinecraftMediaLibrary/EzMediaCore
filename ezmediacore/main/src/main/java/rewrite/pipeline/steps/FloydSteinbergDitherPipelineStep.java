package rewrite.pipeline.steps;

import rewrite.dither.algorithm.error.FloydDither;
import rewrite.dither.load.ColorPalette;
import rewrite.pipeline.frame.DitheredFramePacket;
import rewrite.pipeline.frame.FramePacket;

public final class FloydSteinbergDitherPipelineStep implements FramePipelineStep<FramePacket, DitheredFramePacket> {

  private final FloydDither dither;

  public FloydSteinbergDitherPipelineStep(final ColorPalette palette) {
    this.dither = new FloydDither(palette, false);
  }

  @Override
  public DitheredFramePacket process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    final byte[] dithered = this.dither.standardMinecraftDither(rgb, width);
    return DitheredFramePacket.create(input, dithered);
  }
}
