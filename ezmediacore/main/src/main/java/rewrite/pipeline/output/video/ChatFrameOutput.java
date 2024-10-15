package rewrite.pipeline.output.video;

import rewrite.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import rewrite.dimension.Resolution;
import rewrite.pipeline.output.DelayConfiguration;
import rewrite.pipeline.output.Viewers;
import rewrite.pipeline.output.NamedStringCharacter;
import rewrite.dimension.Dimension;
import rewrite.pipeline.frame.FramePacket;
import rewrite.reflect.PacketToolsProvider;

import java.util.UUID;

public final class ChatFrameOutput extends MinecraftFrameOutput<FramePacket> {

  private final NamedStringCharacter character;
  private volatile long lastUpdated;

  ChatFrameOutput(final EzMediaCore core,
                         final Viewers viewers,
                         final DelayConfiguration configuration,
                         final Dimension resolution,
                         final NamedStringCharacter character) {
    super(core, viewers, resolution, configuration);
    this.character = character;
  }

  public static ChatFrameOutputBuilder builder() {
    return new ChatFrameOutputBuilder();
  }

  @Override
  public void output(final FramePacket input) {
    final long time = System.currentTimeMillis();
    final DelayConfiguration configuration = this.getDelayConfiguration();
    final long delay = configuration.getDelay();
    if (time - this.lastUpdated > delay) {
      final EzMediaCore core = this.getCore();
      final PacketHandler handler = PacketToolsProvider.getPacketHandler();
      final Viewers viewers = this.getViewers();
      final UUID[] uuids = viewers.getViewers();
      final int[] data = input.getRGBSamples();
      final String character = this.character.getCharacter();
      final Dimension resolution = this.getResolution();
      final int width = resolution.getWidth();
      final int height = resolution.getHeight();
      handler.displayChat(uuids, data, character, width, height);
      this.lastUpdated = time;
    }
  }

  public static class ChatFrameOutputBuilder {

    private Viewers viewers = Viewers.onlinePlayers();
    private DelayConfiguration configuration = DelayConfiguration.DELAY_20_MS;
    private Dimension resolution = Resolution.X360_640;
    private NamedStringCharacter character = NamedStringCharacter.TINY_SQUARE;

    public ChatFrameOutputBuilder viewers(final Viewers viewers) {
      this.viewers = viewers;
      return this;
    }

    public ChatFrameOutputBuilder delay(final DelayConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    public ChatFrameOutputBuilder resolution(final Dimension resolution) {
      this.resolution = resolution;
      return this;
    }

    public ChatFrameOutputBuilder character(final NamedStringCharacter character) {
      this.character = character;
      return this;
    }

    public ChatFrameOutput build(final EzMediaCore core) {
      return new ChatFrameOutput(core, this.viewers, this.configuration, this.resolution, this.character);
    }
  }
}
