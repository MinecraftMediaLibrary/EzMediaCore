package io.github.pulsebeat02.ezmediacore.pipeline.output.video;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.pipeline.output.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.pipeline.output.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.FramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.output.FrameOutputSource;

public abstract class MinecraftFrameOutput<T extends FramePacket> implements FrameOutputSource<T> {

  private final EzMediaCore core;
  private final Viewers viewers;
  private final Dimension resolution;
  private final DelayConfiguration configuration;

  public MinecraftFrameOutput(final EzMediaCore core, final Viewers viewers, final Dimension resolution, final DelayConfiguration configuration) {
    this.core = core;
    this.viewers = viewers;
    this.resolution = resolution;
    this.configuration = configuration;
  }

  public Dimension getResolution() {
    return this.resolution;
  }

  public EzMediaCore getCore() {
    return this.core;
  }

  public Viewers getViewers() {
    return this.viewers;
  }

  public DelayConfiguration getDelayConfiguration() {
    return this.configuration;
  }
}
