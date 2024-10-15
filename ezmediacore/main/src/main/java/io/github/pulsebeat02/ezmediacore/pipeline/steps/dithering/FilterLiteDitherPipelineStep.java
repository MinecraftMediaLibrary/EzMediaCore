package io.github.pulsebeat02.ezmediacore.pipeline.steps.dithering;

import io.github.pulsebeat02.ezmediacore.dither.algorithm.error.FilterLiteDither;
import io.github.pulsebeat02.ezmediacore.dither.palette.ColorPalette;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.DitheredFramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.DitheredPacket;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.FramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.steps.FramePipelineStep;

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
