package rewrite.pipeline.output.video;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import rewrite.dimension.BlockDimension;
import rewrite.dimension.Resolution;
import rewrite.pipeline.frame.DitheredFramePacket;
import rewrite.pipeline.output.DelayConfiguration;
import rewrite.pipeline.output.Identifier;
import rewrite.pipeline.output.Viewers;
import rewrite.dimension.Dimension;
import rewrite.pipeline.frame.FramePacket;

import java.util.UUID;

public final class MapFrameOutput extends MinecraftFrameOutput<DitheredFramePacket> {

  private final Dimension blocks;
  private final Identifier<Integer> startingMap;
  private volatile long lastUpdated;

  public MapFrameOutput(
          final EzMediaCore core,
          final Viewers viewers,
          final DelayConfiguration configuration,
          final Dimension resolution,
          final Dimension blocks,
          final Identifier<Integer> startingMap) {
    super(core, viewers, resolution, configuration);
    this.blocks = blocks;
    this.startingMap = startingMap;
  }

  public static MapFrameOutputBuilder builder() {
    return new MapFrameOutputBuilder();
  }

  @Override
  public void output(final DitheredFramePacket input) {
    final DelayConfiguration configuration = this.getDelayConfiguration();
    final long delay = configuration.getDelay();
    final long time = System.currentTimeMillis();
    if (time - this.lastUpdated > delay) {
      final EzMediaCore core = this.getCore();
      final PacketHandler handler = core.getHandler();
      final Viewers viewers = this.getViewers();
      final int blockWidth = this.blocks.getWidth();
      final Dimension resolution = this.getResolution();
      final int height = resolution.getHeight();
      final int width = resolution.getWidth();
      final byte[] data = input.getMapDitheredSamples();
      final int id = this.startingMap.getValue();
      final UUID[] uuids = viewers.getViewers();
      handler.displayMaps(uuids, data, id, width, height, blockWidth);
      this.lastUpdated = time;
    }
  }

  public static class MapFrameOutputBuilder {

    private Viewers viewers = Viewers.onlinePlayers();
    private DelayConfiguration configuration = DelayConfiguration.DELAY_20_MS;
    private Dimension resolution = Resolution.X360_640;
    private Dimension blocks = BlockDimension.X5_5;
    private Identifier<Integer> startingMap;

    public MapFrameOutputBuilder viewers(final Viewers viewers) {
      this.viewers = viewers;
      return this;
    }

    public MapFrameOutputBuilder delay(final DelayConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    public MapFrameOutputBuilder resolution(final Dimension resolution) {
      this.resolution = resolution;
      return this;
    }

    public MapFrameOutputBuilder blocks(final Dimension blocks) {
      this.blocks = blocks;
      return this;
    }

    public MapFrameOutputBuilder startingMap(final Identifier<Integer> startingMap) {
      this.startingMap = startingMap;
      return this;
    }

    public MapFrameOutput build(final EzMediaCore core) {
      return new MapFrameOutput(core, this.viewers, this.configuration, this.resolution, this.blocks, this.startingMap);
    }
  }
}
