/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

  @Override
  public byte @NotNull [] getByteArray() {
    return this.buffer.array();
  }
}
