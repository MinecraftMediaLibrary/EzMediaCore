package io.github.pulsebeat02.ezmediacore.dither.algorithm;

import static io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil.COLOR_MAP;

import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.MapPalette;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.NotNull;

public class RandomDither implements DitherAlgorithm {

  private final ThreadLocalRandom random;

  RandomDither() {
    this.random = ThreadLocalRandom.current();
  }

  @Override
  public @NotNull ByteBuffer ditherIntoMinecraft(final int @NotNull [] buffer, final int width) {
    final int height = buffer.length / width;
    final ByteBuffer data = ByteBuffer.allocate(buffer.length);
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        final int color = buffer[index];
        data.put(
            index,
            this.getBestColor(
                ((color >> 16) & 0xFF) + this.random.nextInt(-64, 65),
                ((color >> 8) & 0xFF) + this.random.nextInt(-64, 65),
                ((color) & 0xFF) + this.random.nextInt(-64, 65)));
      }
    }
    return data;
  }

  @Override
  public void dither(final int @NotNull [] buffer, final int width) {
    final int height = buffer.length / width;
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        final int color = buffer[index];
        buffer[index] =
            this.getBestColorNormal(
                ((color >> 16) & 0xFF) + this.random.nextInt(-64, 65),
                ((color >> 8) & 0xFF) + this.random.nextInt(-64, 65),
                ((color) & 0xFF) + this.random.nextInt(-64, 65));
      }
    }
  }

  private byte getBestColor(final int red, final int green, final int blue) {
    return COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
  }

  private int getBestColorNormal(final int r, final int g, final int b) {
    return MapPalette.getColor(this.getBestColor(r, g, b)).getRGB();
  }
}
