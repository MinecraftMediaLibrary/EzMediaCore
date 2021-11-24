package io.github.pulsebeat02.ezmediacore.callback.buffer;

public interface BufferCarrier {

  byte getByte(final int index);

  int getCapacity();
}
