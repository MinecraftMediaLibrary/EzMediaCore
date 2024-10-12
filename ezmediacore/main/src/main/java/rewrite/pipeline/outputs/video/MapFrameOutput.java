package rewrite.pipeline.outputs.video;

import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.outputs.FrameOutputSource;

public final class MapFrameOutput implements FrameOutputSource<FramePacket> {

  private final int width;
  private final int height;
  private final int blockWidth;
  private final int blockHeight;
  private final int startingMap;

  public MapFrameOutput(final int width, final int height, final int blockWidth, final int blockHeight, final int startingMap) {
    this.width = width;
    this.height = height;
    this.blockWidth = blockWidth;
    this.blockHeight = blockHeight;
    this.startingMap = startingMap;
  }


  @Override
  public void output(final FramePacket input) {
  }
}
