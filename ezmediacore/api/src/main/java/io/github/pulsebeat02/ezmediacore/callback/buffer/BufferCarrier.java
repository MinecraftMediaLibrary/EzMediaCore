package io.github.pulsebeat02.ezmediacore.callback.buffer;

import org.jetbrains.annotations.NotNull;

public interface BufferCarrier {

  byte getByte(final int index);

  int getCapacity();

  byte @NotNull [] getByteArray();
}
