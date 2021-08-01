package io.github.pulsebeat02.ezmediacore.natives;

import java.nio.ByteBuffer;

public final class NativeDitherBuffer {

  static {
    System.loadLibrary("filterlite-dither");
  }

  private native void setup(final int[] colorMap, final int[] fullColorMap);

  private native void dither_native(final ByteBuffer buffer, final int[] data, final int width);
}
