package io.github.pulsebeat02.ezmediacore.dither.load;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import org.jetbrains.annotations.NotNull;

final class LoadGreen extends RecursiveTask<byte[]> {

  @Serial private static final long serialVersionUID = -1221290051151782146L;
  private final int r;
  private final int g;
  private final int[] palette;

  LoadGreen(final int[] palette, final int r, final int g) {
    this.r = r;
    this.g = g;
    this.palette = palette;
  }

  @Override
  protected byte @NotNull [] compute() {
    final List<LoadBlue> blueSub = new ArrayList<>(128);
    for (int b = 0; b < 256; b += 2) {
      final LoadBlue blue = new LoadBlue(this.palette, this.r, this.g, b);
      blueSub.add(blue);
      blue.fork();
    }
    final byte[] matches = new byte[128];
    for (int i = 0; i < 128; i++) {
      matches[i] = blueSub.get(i).join();
    }
    return matches;
  }
}
