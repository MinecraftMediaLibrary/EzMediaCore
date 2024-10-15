package io.github.pulsebeat02.ezmediacore.pipeline.output.video;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import io.github.pulsebeat02.ezmediacore.dimension.BlockDimension;
import io.github.pulsebeat02.ezmediacore.dimension.Resolution;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.DitheredPacket;
import io.github.pulsebeat02.ezmediacore.pipeline.output.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.pipeline.output.Identifier;
import io.github.pulsebeat02.ezmediacore.pipeline.output.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.reflect.PacketToolsProvider;

import java.util.UUID;

public final class MapFrameOutput extends MinecraftFrameOutput<DitheredPacket> {

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
  public void output(final DitheredPacket input) {
    final DelayConfiguration configuration = this.getDelayConfiguration();
    final long delay = configuration.getDelay();
    final long time = System.currentTimeMillis();
    if (time - this.lastUpdated > delay) {
      final EzMediaCore core = this.getCore();
      final PacketHandler handler = PacketToolsProvider.getPacketHandler();
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
