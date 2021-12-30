package io.github.pulsebeat02.ezmediacore.natives;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import io.github.pulsebeat02.ezmediacore.dither.buffer.ByteBufCarrier;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrappedNativeDitherLibrary {

  private DitherLibC library;

  public WrappedNativeDitherLibrary(@NotNull final MediaLibraryCore core) {
    try {
      this.library = DitherLibC.INSTANCE;
    } catch (final Exception e) {
      core.getLogger().error("Your server environment does not support native dithering!");
    }
  }

  public @NotNull BufferCarrier filterLiteDither(
      final int[] colors, final byte[] fullColors, final int[] buffer, final int width) {
    return ByteBufCarrier.ofByteBufCarrier(
        Unpooled.wrappedBuffer(
            this.library
                .filterLiteDither(colors, fullColors, buffer, width)
                .getByteArray(0, buffer.length)));
  }

  public @NotNull BufferCarrier floydSteinbergDither(
      final int[] colors, final byte[] fullColors, final int[] buffer, final int width) {
    return ByteBufCarrier.ofByteBufCarrier(
        Unpooled.wrappedBuffer(
            this.library
                .floydSteinbergDither(colors, fullColors, buffer, width)
                .getByteArray(0, buffer.length)));
  }

  public @NotNull BufferCarrier randomDither(
      final int[] colors,
      final byte[] fullColors,
      final int[] buffer,
      final int width,
      final int weight) {
    return ByteBufCarrier.ofByteBufCarrier(
        Unpooled.wrappedBuffer(
            this.library
                .randomDither(colors, fullColors, buffer, width, weight)
                .getByteArray(0, buffer.length)));
  }

  public @NotNull BufferCarrier simpleDither(
      final int[] colors, final byte[] fullColors, final int[] buffer, final int width) {
    return ByteBufCarrier.ofByteBufCarrier(
        Unpooled.wrappedBuffer(
            this.library
                .simpleDither(colors, fullColors, buffer, width)
                .getByteArray(0, buffer.length)));
  }

  public boolean isSupported() {
    return this.library != null;
  }

  public @Nullable DitherLibC getLibrary() {
    return this.library;
  }
}
