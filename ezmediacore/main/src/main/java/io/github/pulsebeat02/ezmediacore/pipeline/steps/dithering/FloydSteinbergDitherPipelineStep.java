package io.github.pulsebeat02.ezmediacore.pipeline.steps.dithering;

import io.github.pulsebeat02.ezmediacore.dither.algorithm.error.FloydDither;
import io.github.pulsebeat02.ezmediacore.dither.palette.ColorPalette;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.DitheredFramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.DitheredPacket;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.FramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.steps.FramePipelineStep;

public final class FloydSteinbergDitherPipelineStep implements FramePipelineStep<FramePacket, DitheredPacket> {

  private final FloydDither dither;

  public FloydSteinbergDitherPipelineStep(final ColorPalette palette) {
    this.dither = new FloydDither(palette, false);
  }

  @Override
  public DitheredPacket process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    final byte[] dithered = this.dither.standardMinecraftDither(rgb, width);
    return DitheredFramePacket.create(input, dithered);
  }
}
