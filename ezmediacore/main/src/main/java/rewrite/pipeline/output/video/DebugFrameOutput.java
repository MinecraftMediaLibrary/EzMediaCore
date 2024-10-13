package rewrite.pipeline.output.video;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import org.bukkit.Location;
import rewrite.dimension.Resolution;
import rewrite.pipeline.output.DelayConfiguration;
import rewrite.pipeline.output.NamedStringCharacter;
import rewrite.pipeline.output.Viewers;
import rewrite.dimension.Dimension;
import rewrite.pipeline.frame.FramePacket;

import java.util.UUID;

public final class DebugFrameOutput extends MinecraftFrameOutput {

  private final Location location;
  private final NamedStringCharacter character;
  private volatile long lastUpdated;

  DebugFrameOutput(final EzMediaCore core, final Viewers viewers, final Dimension resolution, final DelayConfiguration configuration, final Location location,
                          final NamedStringCharacter character) {
    super(core, viewers, resolution, configuration);
    this.location = location;
    this.character = character;
  }

  public static DebugFrameOutputBuilder builder() {
    return new DebugFrameOutputBuilder();
  }

  @Override
  public void output(final FramePacket input) {
    final long time = System.currentTimeMillis();
    final long delay = this.getDelayConfiguration().getDelay();
    final int z = (int) this.location.getZ();
    final Dimension dimension = this.getResolution();
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();
    if (time - this.lastUpdated >= delay) {
      final Viewers viewers = this.getViewers();
      final UUID[] uuids = viewers.getViewers();
      final EzMediaCore core = this.getCore();
      final PacketHandler handler = core.getHandler();
      final int[] data = input.getRGBSamples();
      final String name = this.character.getCharacter();
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          final int modifiedX = (int) (this.location.getX() - (width / 2D)) + x;
          final int modifiedY = (int) (this.location.getY() + (height / 2D)) - y;
          final int color = data[width * y + x];
          final int newDelay = (int) (delay + 100);
          handler.displayDebugMarker(uuids, name, modifiedY, z, modifiedX, color, newDelay);
        }
      }
      this.lastUpdated = time;
    }
  }

  public static class DebugFrameOutputBuilder {

    private Viewers viewers = Viewers.onlinePlayers();
    private DelayConfiguration configuration = DelayConfiguration.DELAY_20_MS;
    private Dimension resolution = Resolution.X360_640;
    private NamedStringCharacter character = NamedStringCharacter.TINY_SQUARE;

    public DebugFrameOutputBuilder viewers(final Viewers viewers) {
      this.viewers = viewers;
      return this;
    }

    public DebugFrameOutputBuilder delay(final DelayConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    public DebugFrameOutputBuilder resolution(final Dimension resolution) {
      this.resolution = resolution;
      return this;
    }

    public DebugFrameOutputBuilder character(final NamedStringCharacter character) {
      this.character = character;
      return this;
    }

    public DebugFrameOutput build(final EzMediaCore core, final Location location) {
      return new DebugFrameOutput(core, this.viewers, this.resolution, this.configuration, location, this.character);
    }
  }
}
