package io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered;

import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.buffer.ByteBufCarrier;
import io.github.pulsebeat02.ezmediacore.utility.graphics.DitherUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/** See https://github.com/makeworld-the-better-one/dither/blob/master/pixelmappers.go */
public final class OrderedDither implements DitherAlgorithm {

  private final float[][] precalc;
  private final int xdim;
  private final int ydim;

  @Contract(pure = true)
  public OrderedDither(@NotNull final OrderedPixelMapper mapper) {
    this.precalc = mapper.getMatrix();
    this.ydim = this.precalc.length;
    this.xdim = this.precalc[0].length;
  }

  @Override
  public @NotNull BufferCarrier ditherIntoMinecraft(final int @NotNull [] buffer, final int width) {
    final int length = buffer.length;
    final int height = length / width;
    final ByteBuf data = Unpooled.buffer(length);
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        final int color = buffer[index];
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        r = (r += this.precalc[y % this.ydim][x % this.xdim]) > 255 ? 255 : Math.max(r, 0);
        g = (g += this.precalc[y % this.ydim][x % this.xdim]) > 255 ? 255 : Math.max(g, 0);
        b = (b += this.precalc[y % this.ydim][x % this.xdim]) > 255 ? 255 : Math.max(b, 0);
        data.writeByte(DitherUtils.getBestColor(r, g, b));
      }
    }
    return ByteBufCarrier.ofByteBufCarrier(data);
  }

  @Override
  public void dither(final int @NotNull [] buffer, final int width) {
    final int height = buffer.length / width;
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        final int color = buffer[index];
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        r = (r += this.precalc[y % this.ydim][x % this.xdim]) > 255 ? 255 : Math.max(r, 0);
        g = (g += this.precalc[y % this.ydim][x % this.xdim]) > 255 ? 255 : Math.max(g, 0);
        b = (b += this.precalc[y % this.ydim][x % this.xdim]) > 255 ? 255 : Math.max(b, 0);
        buffer[index] = DitherUtils.getBestColorNormal(r, g, b);
      }
    }
  }
}
