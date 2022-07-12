package io.github.pulsebeat02.ezmediacore.dither.buffer;

import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ByteBufCarrier implements BufferCarrier {

  private final ByteBuf buffer;

  ByteBufCarrier(@NotNull final ByteBuf buffer) {
    this.buffer = buffer;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull ByteBufCarrier ofByteBufCarrier(@NotNull final ByteBuf buffer) {
    return new ByteBufCarrier(buffer);
  }


  @Contract(value = "_ -> new", pure = true)
  public static @NotNull ByteBufCarrier ofByteBufCarrier(@NotNull final ByteBuffer buffer) {
    return new ByteBufCarrier(Unpooled.copiedBuffer(buffer));
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull ByteBufCarrier ofByteArray(final byte @NotNull [] buffer) {
    return new ByteBufCarrier(Unpooled.wrappedBuffer(buffer));
  }

  @Override
  public byte getByte(final int index) {
    return this.buffer.getByte(index);
  }

  @Override
  public int getCapacity() {
    return this.buffer.capacity();
  }
}
