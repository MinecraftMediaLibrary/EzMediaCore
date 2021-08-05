package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.dimension.ImmutableDimension;
import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import java.util.Collection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MapCallback extends FrameCallback implements MapCallbackDispatcher {

  private final DitherAlgorithm algorithm;
  private final int map;

  public MapCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final ImmutableDimension dimension,
      @NotNull final Collection<? extends Player> viewers,
      final DitherAlgorithm algorithm,
      final int map,
      final int blockWidth,
      final int delay) {
    super(core, dimension, viewers, blockWidth, delay);
    this.algorithm = algorithm;
    this.map = map;
  }

  @Override
  public void process(final int[] data) {
    final long time = System.currentTimeMillis();
    final ImmutableDimension dimension = this.getDimensions();
    if (time - this.getLastUpdated() >= this.getFrameDelay()) {
      this.setLastUpdated(time);
      final int width = this.getBlockWidth();
      this.getPacketHandler()
          .displayMaps(
              this.getViewers(),
              this.map,
              dimension.getWidth(),
              this.getDimensions().getHeight(),
              this.algorithm.ditherIntoMinecraft(data, width),
              width);
    }
  }

  @Override
  public long getMapId() {
    return this.map;
  }

  @Override
  public @NotNull DitherAlgorithm getAlgorithm() {
    return this.algorithm;
  }
}
