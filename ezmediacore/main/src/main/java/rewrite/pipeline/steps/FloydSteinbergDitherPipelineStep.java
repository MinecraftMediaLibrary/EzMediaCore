package rewrite.pipeline.steps;

import rewrite.dither.algorithm.error.FloydDither;
import rewrite.dither.load.ColorPalette;
import rewrite.pipeline.frame.FramePacket;

public final class FloydSteinbergDitherPipelineStep implements FramePipelineStep<FramePacket, FramePacket> {

  private final FloydDither dither;

  public FloydSteinbergDitherPipelineStep(final ColorPalette palette) {
    this.dither = new FloydDither(palette, false);
  }

  @Override
  public FramePacket process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    this.dither.dither(rgb, width);
    return input;
  }
}
