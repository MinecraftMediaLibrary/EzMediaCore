package io.github.pulsebeat02.ezmediacore.natives;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.jetbrains.annotations.NotNull;

public interface DitherLibC extends Library {

  DitherLibC INSTANCE = Native.load("dither", DitherLibC.class);

  @NotNull
  Pointer filterLiteDither(
      final int[] colors, final byte[] fullColors, final int[] buffer, final int width);

  @NotNull
  Pointer floydSteinbergDither(
      final int[] colors, final byte[] fullColors, final int[] buffer, final int width);

  @NotNull
  Pointer randomDither(
      final int[] colors, final byte[] fullColors, final int[] buffer, final int width, final int weight);

  @NotNull
  Pointer simpleDither(
      final int[] colors, final byte[] fullColors, final int[] buffer, final int width);
}
