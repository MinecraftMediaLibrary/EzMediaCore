package io.github.pulsebeat02.epicmedialib.dither.algorithm;

import io.github.pulsebeat02.epicmedialib.dither.DitherAlgorithm;
import io.github.pulsebeat02.epicmedialib.dither.MapPalette;
import java.nio.ByteBuffer;
import org.jetbrains.annotations.NotNull;

import static io.github.pulsebeat02.epicmedialib.dither.DitherLookupUtil.COLOR_MAP;

public class OrderedDither implements DitherAlgorithm {

  private static final float[][] BAYER_MATRIX_TWO;
  private static final float[][] BAYER_MATRIX_FOUR;
  private static final float[][] BAYER_MATRIX_EIGHT;

  static {
    BAYER_MATRIX_TWO =
        new float[][] {
          {1f, 3f},
          {4f, 2f},
        };

    BAYER_MATRIX_FOUR =
        new float[][] {
          {1f, 9f, 3f, 11f},
          {13f, 5f, 15f, 7f},
          {4f, 12f, 2f, 10f},
          {16f, 8f, 14f, 6f}
        };

    BAYER_MATRIX_EIGHT =
        new float[][] {
          {1f, 49f, 13f, 61f, 4f, 52f, 16f, 64f},
          {33f, 17f, 45f, 29f, 36f, 20f, 48f, 32f},
          {9f, 57f, 5f, 53f, 12f, 60f, 8f, 56f},
          {41f, 25f, 37f, 21f, 44f, 28f, 40f, 24f},
          {3f, 51f, 15f, 63f, 2f, 50f, 14f, 62f},
          {35f, 19f, 47f, 31f, 34f, 18f, 46f, 30f},
          {11f, 59f, 7f, 55f, 10f, 58f, 6f, 54f},
          {43f, 27f, 39f, 23f, 42f, 26f, 38f, 22f}
        };
  }

  private final float correction;

  private float[][] matrix;
  private float multiplicative;
  private int size;

  public OrderedDither(@NotNull final DitherType type) {
    switch (type) {
      case TWO:
        this.matrix = BAYER_MATRIX_TWO;
        this.size = 2;
        this.multiplicative = 0.25f;
        break;
      case FOUR:
        this.matrix = BAYER_MATRIX_FOUR;
        this.size = 4;
        this.multiplicative = 0.0625f;
        break;
      case EIGHT:
        this.matrix = BAYER_MATRIX_EIGHT;
        this.size = 8;
        this.multiplicative = 0.015625f;
        break;
    }
    this.correction = 255f / (this.size * this.size);
    convertToFloat();
  }

  public static float[][] getBayerMatrixTwo() {
    return BAYER_MATRIX_TWO;
  }

  public static float[][] getBayerMatrixFour() {
    return BAYER_MATRIX_FOUR;
  }

  public static float[][] getBayerMatrixEight() {
    return BAYER_MATRIX_EIGHT;
  }

  private int getBestColorNormal(final int rgb) {
    return MapPalette.getColor(getBestColor(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF))
        .getRGB();
  }

  private byte getBestColor(final int rgb) {
    return COLOR_MAP[
        (rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
  }

  private byte getBestColor(final int red, final int green, final int blue) {
    return COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
  }

  private void convertToFloat() {
    for (int i = 0; i < this.matrix.length; i++) {
      for (int j = 0; j < this.matrix[i].length; j++) {
        this.matrix[i][j] = this.matrix[i][j] * this.multiplicative - 0.5f;
      }
    }
  }

  @Override
  public void dither(final int[] buffer, final int width) {
    final int height = buffer.length / width;
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        buffer[index] =
            getBestColorNormal(
                (int)
                    (buffer[index]
                        + this.correction * ((this.matrix[x % this.size][y % this.size] - 0.5))));
      }
    }
  }

  @Override
  public ByteBuffer ditherIntoMinecraft(final int[] buffer, final int width) {
    final int height = buffer.length / width;
    final ByteBuffer data = ByteBuffer.allocate(buffer.length);
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        data.put(
            getBestColor(
                (int)
                    (buffer[yIndex + x]
                        + this.correction * ((this.matrix[x % this.size][y % this.size] - 0.5)))));
      }
    }
    return data;
  }

  public float[][] getMatrix() {
    return this.matrix;
  }

  public float getCorrection() {
    return this.correction;
  }

  public float getMultiplicative() {
    return this.multiplicative;
  }

  public int getSize() {
    return this.size;
  }

  public enum DitherType {
    TWO,
    FOUR,
    EIGHT
  }
}
