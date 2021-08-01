package io.github.pulsebeat02.epicmedialib.callback;

import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import io.github.pulsebeat02.epicmedialib.utility.ImmutableDimension;
import java.util.UUID;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class BlockHighlightCallback extends FrameCallback
    implements BlockHighlightCallbackDispatcher {

  private final Location location;

  public BlockHighlightCallback(
      @NotNull final MediaLibraryCore core,
      final UUID[] viewers,
      @NotNull final Location location,
      @NotNull final ImmutableDimension dimension,
      final int blockWidth,
      final int delay) {
    super(core, viewers, dimension, blockWidth, delay);
    this.location = location;
  }

  @Override
  public void process(final int[] data) {
    final long time = System.currentTimeMillis();
    final int delay = getFrameDelay();
    if (time - getLastUpdated() >= delay) {
      setLastUpdated(time);
      final ImmutableDimension dimension = getDimensions();
      final int width = dimension.getWidth();
      final int height = dimension.getHeight();
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          getPacketHandler()
              .displayDebugMarker(
                  getViewers(),
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
