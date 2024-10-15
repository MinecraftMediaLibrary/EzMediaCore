package rewrite.image;

import rewrite.dither.DitherAlgorithm;
import rewrite.dither.algorithm.error.FilterLiteDither;

import java.awt.image.BufferedImage;

public final class BasicDitheredImage implements DitheredImage {

  private final BufferedImage internal;

  BasicDitheredImage(final BufferedImage internal) {
    this.internal = internal;
  }

  public static BasicDitheredImage create(final BufferedImage image) {
    final FilterLiteDither algorithm = new FilterLiteDither();
    return create(image, algorithm);
  }

  public static BasicDitheredImage create(final BufferedImage image, final DitherAlgorithm algorithm) {
    final int width = image.getWidth();
    final int height = image.getHeight();
    final int[] rgbSamples = new int[width * height];
    image.getRGB(0, 0, width, height, rgbSamples, 0, width);
    algorithm.dither(rgbSamples, width);
    image.setRGB(0, 0, width, height, rgbSamples, 0, width);
    return new BasicDitheredImage(image);
  }

  @Override
  public BufferedImage getInternal() {
    return this.internal;
  }
}
