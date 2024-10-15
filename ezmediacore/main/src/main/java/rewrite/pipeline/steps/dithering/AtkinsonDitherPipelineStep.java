package rewrite.pipeline.steps.dithering;

import rewrite.dither.algorithm.error.AtkinsonDither;
import rewrite.dither.load.ColorPalette;
import rewrite.pipeline.frame.DitheredFramePacket;
import rewrite.pipeline.frame.DitheredPacket;
import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.steps.FramePipelineStep;

public final class AtkinsonDitherPipelineStep implements FramePipelineStep<FramePacket, DitheredPacket> {

  private final AtkinsonDither dither;

  public AtkinsonDitherPipelineStep(final ColorPalette palette) {
    this.dither = new AtkinsonDither(palette);
  }

  @Override
  public DitheredPacket process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    final byte[] dithered = this.dither.standardMinecraftDither(rgb, width);
    return DitheredFramePacket.create(input, dithered);
  }
}