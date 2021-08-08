package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.dimension.ImmutableDimension;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BlockHighlightCallback extends FrameCallback
    implements BlockHighlightCallbackDispatcher {

  private final Location location;

  public BlockHighlightCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final ImmutableDimension dimension,
      @NotNull final Collection<? extends Player> viewers,
      @NotNull final Location location,
      final int blockWidth,
      final int delay) {
    super(core, dimension, viewers, blockWidth, delay);
    this.location = location;
  }

  @Override
  public void process(final int[] data) {
    final long time = System.currentTimeMillis();
    final int delay = this.getFrameDelay();
    if (time - this.getLastUpdated() >= delay) {
      this.setLastUpdated(time);
      final ImmutableDimension dimension = this.getDimensions();
      final int width = dimension.width();
      final int height = dimension.height();
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          this.getPacketHandler()
              .displayDebugMarker(
                  this.getViewers(),
                  (int) (this.location.getX() - (width / 2D)) + x,
                  (int) (this.location.getY() + (height / 2D)) - y,
                  (int) this.location.getZ(),
                  data[width * y + x],
                  delay + 100);
        }
      }
    }
  }

  @Override
  public @NotNull Location getLocation() {
    return this.location;
  }
}
