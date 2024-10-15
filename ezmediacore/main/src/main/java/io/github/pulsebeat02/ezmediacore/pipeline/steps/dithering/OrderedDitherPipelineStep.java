package io.github.pulsebeat02.ezmediacore.pipeline.steps.dithering;

import io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.OrderedDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.OrderedPixelMapper;
import io.github.pulsebeat02.ezmediacore.dither.palette.ColorPalette;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.DitheredFramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.DitheredPacket;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.FramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.steps.FramePipelineStep;

public final class OrderedDitherPipelineStep implements FramePipelineStep<FramePacket, DitheredPacket> {

  private final OrderedDither dither;

  public OrderedDitherPipelineStep(final ColorPalette palette, final OrderedPixelMapper mapper) {
    this.dither = new OrderedDither(palette, mapper);
  }

  @Override
  public DitheredPacket process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    final byte[] dithered = this.dither.ditherIntoMinecraft(rgb, width);
    return DitheredFramePacket.create(input, dithered);
  }
}
