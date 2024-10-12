package rewrite.pipeline.steps;

import io.github.pulsebeat02.ezmediacore.dither.algorithm.error.FloydDither;
import rewrite.pipeline.frame.FramePacket;

public final class FloydSteinbergDitherPipelineStep implements FramePipelineStep<FramePacket, FramePacket> {

  private final FloydDither dither;

  public FloydSteinbergDitherPipelineStep() {
    this.dither = new FloydDither();
  }

  @Override
  public FramePacket process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    dither.dither(rgb, width);
    return input;
  }
}
