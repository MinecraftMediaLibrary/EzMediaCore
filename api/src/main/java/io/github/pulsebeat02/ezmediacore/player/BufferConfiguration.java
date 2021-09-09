package io.github.pulsebeat02.ezmediacore.player;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class BufferConfiguration {

  public static BufferConfiguration BUFFER_5;
  public static BufferConfiguration BUFFER_10;
  public static BufferConfiguration BUFFER_15;
  public static BufferConfiguration BUFFER_20;

  static {
    BUFFER_5 = ofBuffer(5);
    BUFFER_10 = ofBuffer(10);
    BUFFER_15 = ofBuffer(15);
    BUFFER_20 = ofBuffer(20);
  }

  private final int buffer;

  BufferConfiguration(final int buffer) {
    this.buffer = buffer;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull BufferConfiguration ofBuffer(final int buffer) {
    return new BufferConfiguration(buffer);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull BufferConfiguration ofBuffer(@NotNull final Number number) {
    return ofBuffer((int) number);
  }

  public int getBuffer() {
    return this.buffer;
  }
}
