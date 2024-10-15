package io.github.pulsebeat02.ezmediacore.resourcepack.wrapper;

public interface PackJSON {
  void write(final String property, final Object value);
  byte[] serialize();
}
