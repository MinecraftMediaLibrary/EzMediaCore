package rewrite.pipeline.outputs.video;

import rewrite.dimension.Dimension;
import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.outputs.FrameOutputSource;

public final class MapFrameOutput implements FrameOutputSource<FramePacket> {

  private final Dimension resolution;
  private final Dimension blocks;
  private final int startingMap;

  public MapFrameOutput(final Dimension resolution, final Dimension blocks, final int startingMap) {
    this.resolution = resolution;
    this.blocks = blocks;
    this.startingMap = startingMap;
  }

  @Override
  public void output(final FramePacket input) {
  }
}
