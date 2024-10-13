package rewrite.pipeline.steps;

import rewrite.dither.algorithm.error.FilterLiteDither;
import rewrite.pipeline.frame.FramePacket;

public final class FilterLiteDitherPipelineStep implements FramePipelineStep<FramePacket, FramePacket> {

  private final FilterLiteDither dither;

  public FilterLiteDitherPipelineStep() {
    this.dither = new FilterLiteDither();
  }

  @Override
  public FramePacket process(final FramePacket input) {
    final int[] rgb = input.getRGBSamples();
    final int width = input.getImageWidth();
    this.dither.dither(rgb, width);
    return input;
  }
}
