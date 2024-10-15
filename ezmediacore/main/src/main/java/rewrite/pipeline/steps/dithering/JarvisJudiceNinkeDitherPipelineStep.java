package rewrite.pipeline.steps.dithering;

import rewrite.dither.algorithm.error.JarvisJudiceNinkeDither;
import rewrite.dither.load.ColorPalette;
import rewrite.pipeline.frame.DitheredFramePacket;
import rewrite.pipeline.frame.DitheredPacket;
import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.steps.FramePipelineStep;

public final class JarvisJudiceNinkeDitherPipelineStep implements FramePipelineStep<FramePacket, DitheredPacket> {

  private final JarvisJudiceNinkeDither dither;

  public JarvisJudiceNinkeDitherPipelineStep(final ColorPalette palette) {
    this.dither = new JarvisJudiceNinkeDither(palette);
  }

  @Override
  public DitheredPacket process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    final byte[] dithered = this.dither.standardMinecraftDither(rgb, width);
    return DitheredFramePacket.create(input, dithered);
  }
}