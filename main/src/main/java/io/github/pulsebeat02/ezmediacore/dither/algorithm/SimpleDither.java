package io.github.pulsebeat02.ezmediacore.dither.algorithm;

import static io.github.pulsebeat02.ezmediacore.dither.DitherLookupUtil.COLOR_MAP;

import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.MapPalette;
import java.nio.ByteBuffer;

public class SimpleDither implements DitherAlgorithm {

  @Override
  public void dither(final int[] buffer, final int width) {
    final int height = buffer.length / width;
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        buffer[index] = getBestColorNormal(buffer[index]);
      }
    }
  }

  @Override
  public ByteBuffer ditherIntoMinecraft(final int[] buffer, final int width) {
    final int height = buffer.length / width;
    final ByteBuffer data = ByteBuffer.allocate(buffer.length);
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        data.put(getBestColor(buffer[yIndex + x]));
      }
    }
    return data;
  }

  private byte getBestColor(final int red, final int green, final int blue) {
    return COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
  }

  private byte getBestColor(final int rgb) {
    return COLOR_MAP[
        (rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
  }

  private int getBestColorNormal(final int rgb) {
    return MapPalette.getColor(getBestColor(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF))
        .getRGB();
  }
}
