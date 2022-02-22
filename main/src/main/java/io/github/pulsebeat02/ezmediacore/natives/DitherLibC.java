package io.github.pulsebeat02.ezmediacore.natives;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DitherLibC extends Library {

  DitherLibC INSTANCE = getInstance0();

  @NotNull
  Pointer filterLiteDither(
      final int[] colors, final byte[] fullColors, final int[] buffer, final int width);

  @NotNull
  Pointer floydSteinbergDither(
      final int[] colors, final byte[] fullColors, final int[] buffer, final int width);

  @NotNull
  Pointer randomDither(
      final int[] colors,
      final byte[] fullColors,
      final int[] buffer,
      final int width,
      final int weight);

  @NotNull
  Pointer simpleDither(
      final int[] colors, final byte[] fullColors, final int[] buffer, final int width);

  private static @Nullable DitherLibC getInstance0() {
    try {
      return Native.load("dither", DitherLibC.class);
    } catch (final UnsatisfiedLinkError ignored) { // suppress as native libraries aren't supported
    }
    return null;
  }

  static boolean isSupported() {
    return INSTANCE != null;
  }
}
