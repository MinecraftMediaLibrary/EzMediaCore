package rewrite.pipeline.steps;

import rewrite.dither.algorithm.error.FilterLiteDither;
import rewrite.dither.load.ColorPalette;
import rewrite.pipeline.frame.DitheredFramePacket;
import rewrite.pipeline.frame.DitheredPacket;
import rewrite.pipeline.frame.FramePacket;

public final class FilterLiteDitherPipelineStep implements FramePipelineStep<FramePacket, DitheredPacket> {

  private final FilterLiteDither dither;

  public FilterLiteDitherPipelineStep(final ColorPalette palette) {
    this.dither = new FilterLiteDither(palette, false);
  }

  @Override
  public DitheredPacket process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    final byte[] dithered = this.dither.standardMinecraftDither(rgb, width);
    return DitheredFramePacket.create(input, dithered);
  }
}
