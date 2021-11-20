package io.github.pulsebeat02.ezmediacore.junit;

import static io.github.pulsebeat02.ezmediacore.dither.algorithm.DitherAlgorithmProvider.FILTER_LITE;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.DitherAlgorithmProvider.FLOYD_STEINBERG;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.DitherAlgorithmProvider.ORDERED_2X2;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.DitherAlgorithmProvider.ORDERED_4X4;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.DitherAlgorithmProvider.ORDERED_8X8;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.DitherAlgorithmProvider.RANDOM;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.DitherAlgorithmProvider.SIMPLE;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil;
import io.github.pulsebeat02.ezmediacore.utility.graphics.VideoFrameUtils;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public final class DitheringAlgorithms {

  private static final int[] BUFFER;

  static {
    try {
      BUFFER = VideoFrameUtils.getBuffer(ImageIO.read(new URL("https://github.com/MinecraftMediaLibrary/EzMediaCore/blob/master/deluxemediaplugin.png")));
    } catch (final IOException e) {
      throw new AssertionError("Error retrieving image resource for dither test!");
    }
    DitherLookupUtil.init();
  }

  @Test
  public void filterLite() {
    this.testDither(FILTER_LITE);
  }

  @Test
  public void floydDither() {
    this.testDither(FLOYD_STEINBERG);
  }

  @Test
  public void orderedDither() {
    this.testDither(ORDERED_2X2);
    this.testDither(ORDERED_4X4);
    this.testDither(ORDERED_8X8);
  }

  @Test
  public void randomDither() {
    this.testDither(RANDOM);
  }

  @Test
  public void simpleDither() {
    this.testDither(SIMPLE);
  }

  private void testDither(@NotNull final DitherAlgorithm algorithm) {
    final int[] copy = Arrays.copyOf(BUFFER, BUFFER.length);
    algorithm.dither(copy, 630);
    assertFalse(Arrays.equals(copy, BUFFER));
  }
}
