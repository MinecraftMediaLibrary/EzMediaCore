package io.github.pulsebeat02.ezmediacore.callback;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import io.github.pulsebeat02.ezmediacore.utility.ImmutableDimension;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public abstract class FrameCallback implements Callback {

  private final MediaLibraryCore core;
  private final ImmutableDimension dimension;
  private final UUID[] viewers;
  private final int blockWidth;

  private final int delay;
  private long lastUpdated;

  // core, viewers, pixels in width, pixels in height, block width, and delay between each frame
  public FrameCallback(
      @NotNull final MediaLibraryCore core,
      final UUID[] viewers,
      @NotNull final ImmutableDimension dimension,
      final int blockWidth,
      final int delay) {
    Preconditions.checkArgument(
        dimension.getWidth() >= 0, "Width must be greater than or equal to 0!");
    Preconditions.checkArgument(
        dimension.getHeight() >= 0, "Height must be greater than or equal to 0!");
    Preconditions.checkArgument(
        delay >= 0, "Delay between frames must be greater than or equal to 0!");
    this.core = core;
    this.viewers = viewers;
    this.dimension = dimension;
    this.blockWidth = blockWidth;
    this.delay = delay;
  }

  @Override
  public int getBlockWidth() {
    return this.blockWidth;
  }

  @Override
  public int getFrameDelay() {
    return this.delay;
  }

  @Override
  public long getLastUpdated() {
    return this.lastUpdated;
  }

  @Override
  public void setLastUpdated(final long lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public @NotNull PacketHandler getPacketHandler() {
    return this.core.getHandler();
  }

  @Override
  public @NotNull UUID[] getViewers() {
    return this.viewers;
  }

  @Override
  public @NotNull ImmutableDimension getDimensions() {
    return this.dimension;
  }
}
