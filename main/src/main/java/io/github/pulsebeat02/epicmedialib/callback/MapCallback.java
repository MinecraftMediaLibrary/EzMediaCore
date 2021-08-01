package io.github.pulsebeat02.epicmedialib.callback;

import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import io.github.pulsebeat02.epicmedialib.dither.DitherAlgorithm;
import io.github.pulsebeat02.epicmedialib.utility.ImmutableDimension;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class MapCallback extends FrameCallback implements MapCallbackDispatcher {

  private final DitherAlgorithm algorithm;
  private final int map;

  public MapCallback(
      @NotNull final MediaLibraryCore core,
      final UUID[] viewers,
      final DitherAlgorithm algorithm,
      final int map,
      @NotNull final ImmutableDimension dimension,
      final int blockWidth,
      final int delay) {
    super(core, viewers, dimension, blockWidth, delay);
    this.algorithm = algorithm;
    this.map = map;
  }

  @Override
  public void process(final int[] data) {
    final long time = System.currentTimeMillis();
    final ImmutableDimension dimension = getDimensions();
    if (time - getLastUpdated() >= getFrameDelay()) {
      setLastUpdated(time);
      final int width = getBlockWidth();
      getPacketHandler()
          .displayMaps(
              getViewers(),
              this.map,
              dimension.getWidth(),
              getDimensions().getHeight(),
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
