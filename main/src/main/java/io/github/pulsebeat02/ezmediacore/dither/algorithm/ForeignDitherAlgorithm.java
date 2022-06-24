package io.github.pulsebeat02.ezmediacore.dither.algorithm;

import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import io.github.pulsebeat02.ezmediacore.dither.NativeDitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.natives.DitherLibC;
import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;

public abstract class ForeignDitherAlgorithm implements NativeDitherAlgorithm {

  private final BiFunction<int[], Integer, BufferCarrier> function;

  public ForeignDitherAlgorithm(final boolean useNative) {
    if (useNative) {
      this.tryUsingNative();
    }
    this.function = useNative ? this::ditherIntoMinecraftNatively : this::standardMinecraftDither;
  }

  public ForeignDitherAlgorithm() {
    this(false);
  }

  private void tryUsingNative() {
    if (!DitherLibC.isSupported()) {
      this.throwException();
    }
  }

  private void throwException() {
    throw new UnsupportedOperationException(
        "Your current platform does not support native dithering!");
  }

  @NotNull
  public abstract BufferCarrier standardMinecraftDither(int @NotNull [] buffer, int width);

  @Override
  public @NotNull BufferCarrier ditherIntoMinecraft(final int @NotNull [] buffer, final int width) {
    return this.function.apply(buffer, width);
  }
}
