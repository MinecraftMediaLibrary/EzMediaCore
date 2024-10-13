package rewrite.pipeline.output.video;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import rewrite.pipeline.output.DelayConfiguration;
import rewrite.pipeline.output.Viewers;
import rewrite.dimension.Dimension;
import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.output.FrameOutputSource;

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
