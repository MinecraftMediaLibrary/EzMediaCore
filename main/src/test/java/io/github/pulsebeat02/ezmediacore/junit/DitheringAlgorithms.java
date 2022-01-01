package io.github.pulsebeat02.ezmediacore.junit;

import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_4X4;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_4X4_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_6X6;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_6X6_2;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_6X6_2_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_6X6_3;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_6X6_3_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_6X6_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_8X8;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_8X8_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_16X16;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_16X16_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_6X6;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_6X6_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_8X8;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_8X8_2;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_8X8_2_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_8X8_3;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_8X8_3_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_DIAGONAL_8X8_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_HORIZONTAL_LINE;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_HORIZONTAL_LINE_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_SPIRAL_5X5;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_SPIRAL_5X5_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_VERTICAL_LINE;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.CLUSTERED_DOT_VERTICAL_LINE_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.HORIZONTAL_3X5;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.HORIZONTAL_3X5_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.NORMAL_2X2;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.NORMAL_2X2_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.NORMAL_4X4;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.NORMAL_4X4_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.NORMAL_8X8;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.NORMAL_8X8_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.VERTICAL_5X3;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.VERTICAL_5X3_MAX;
import static io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.BayerMatrices.createBayerMatrix;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.error.FilterLiteDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.error.FloydDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.OrderedDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.ordered.OrderedPixelMapper;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.random.RandomDither;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.simple.SimpleDither;
import io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil;
import io.github.pulsebeat02.ezmediacore.utility.graphics.VideoFrameUtils;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public final class DitheringAlgorithms {

  private static final int[] BUFFER;

  static {
    try {
      BUFFER =
          VideoFrameUtils.getBuffer(
              ImageIO.read(
                  new URL(
                      "https://github.com/MinecraftMediaLibrary/EzMediaCore/raw/master/deluxemediaplugin.png")));
    } catch (final IOException e) {
      throw new AssertionError("Error retrieving image resource for dither test!");
    }
    DitherLookupUtil.init();
  }

  @Test
  public static void main(final String[] args) {
    final DitheringAlgorithms algorithms = new DitheringAlgorithms();
    algorithms.filterLite();
    algorithms.floydDither();
    algorithms.randomDither();
    algorithms.simpleDither();
    algorithms.orderedDither();
  }

  public void filterLite() {
    this.dither(new FilterLiteDither(), "filter-lite-dithering");
  }

  public void floydDither() {
    this.dither(new FloydDither(), "floyd-steinberg-dithering");
  }

  public void orderedDither() {
    final float strength = 0.005f;
    this.dither(this.createOrderedDither(NORMAL_2X2, NORMAL_2X2_MAX, strength), "2x2 Bayer");
    this.dither(this.createOrderedDither(NORMAL_4X4, NORMAL_4X4_MAX, strength), "4x4 Bayer");
    this.dither(this.createOrderedDither(NORMAL_8X8, NORMAL_8X8_MAX, strength), "8x8 Bayer");
    this.dither(
        this.createOrderedDither(CLUSTERED_DOT_4X4, CLUSTERED_DOT_4X4_MAX, strength),
        "Clustered Dot 4x4");
    this.dither(
        this.createOrderedDither(
            CLUSTERED_DOT_DIAGONAL_8X8, CLUSTERED_DOT_DIAGONAL_8X8_MAX, strength),
        "Clustered Dot Diagonal 8x8");
    this.dither(this.createOrderedDither(VERTICAL_5X3, VERTICAL_5X3_MAX, strength), "Vertical 5x3");
    this.dither(
        this.createOrderedDither(HORIZONTAL_3X5, HORIZONTAL_3X5_MAX, strength), "Horizontal 3x5");
    this.dither(
        this.createOrderedDither(
            CLUSTERED_DOT_DIAGONAL_6X6, CLUSTERED_DOT_DIAGONAL_6X6_MAX, strength),
        "Clustered Dot Diagonal 6x6");
    this.dither(
        this.createOrderedDither(
            CLUSTERED_DOT_DIAGONAL_8X8_2, CLUSTERED_DOT_DIAGONAL_8X8_2_MAX, strength),
        "Clustered Dot Diagonal 8x8 2");
    this.dither(
        this.createOrderedDither(
            CLUSTERED_DOT_DIAGONAL_16X16, CLUSTERED_DOT_DIAGONAL_16X16_MAX, strength),
        "Clustered Dot Diagonal 16x16");
    this.dither(
        this.createOrderedDither(CLUSTERED_DOT_6X6, CLUSTERED_DOT_6X6_MAX, strength),
        "Clustered Dot 6x6");
    this.dither(
        this.createOrderedDither(CLUSTERED_DOT_SPIRAL_5X5, CLUSTERED_DOT_SPIRAL_5X5_MAX, strength),
        "Clustered Dot Spiral 5x5");
    this.dither(
        this.createOrderedDither(
            CLUSTERED_DOT_HORIZONTAL_LINE, CLUSTERED_DOT_HORIZONTAL_LINE_MAX, strength),
        "Clustered Dot Horizontal Line");
    this.dither(
        this.createOrderedDither(
            CLUSTERED_DOT_VERTICAL_LINE, CLUSTERED_DOT_VERTICAL_LINE_MAX, strength),
        "Clustered Dot Vertical Line");
    this.dither(
        this.createOrderedDither(CLUSTERED_DOT_8X8, CLUSTERED_DOT_8X8_MAX, strength),
        "Clustered Dot 8x8");
    this.dither(
        this.createOrderedDither(CLUSTERED_DOT_6X6_2, CLUSTERED_DOT_6X6_2_MAX, strength),
        "Clustered Dot 6x6 2");
    this.dither(
        this.createOrderedDither(CLUSTERED_DOT_6X6_3, CLUSTERED_DOT_6X6_3_MAX, strength),
        "Clustered Dot 6x6 3");
    this.dither(
        this.createOrderedDither(
            CLUSTERED_DOT_DIAGONAL_8X8_3, CLUSTERED_DOT_DIAGONAL_8X8_3_MAX, strength),
        "Clustered Dot Diagonal 8x8 3");
    this.dither(
        this.createOrderedDither(createBayerMatrix(16, 16), 16 * 16, strength), "16x16 Bayer");
    this.dither(
        this.createOrderedDither(createBayerMatrix(32, 32), 32 * 32, strength), "32x32 Bayer");
    this.dither(
        this.createOrderedDither(createBayerMatrix(64, 64), 64 * 64, strength), "64x64 Bayer");
  }

  private OrderedDither createOrderedDither(
      final int[][] matrix, final int max, final float strength) {
    return new OrderedDither(OrderedPixelMapper.ofPixelMapper(matrix, max, strength));
  }

  public void randomDither() {
    this.dither(new RandomDither(RandomDither.NORMAL_WEIGHT), "random-dithering");
  }

  public void simpleDither() {
    this.dither(new SimpleDither(), "simple-dithering");
  }

  private void dither(@NotNull final DitherAlgorithm algorithm, @NotNull final String name) {
    final int[] copy = Arrays.copyOf(BUFFER, BUFFER.length);
    algorithm.dither(copy, 630);
    this.createFrame(copy, name);
    this.assertData(copy);
  }

  private void assertData(final int[] data) {
    assertFalse(Arrays.equals(data, BUFFER));
  }

  private void createFrame(final int[] data, final String name) {
    final int width = 630;

    final BufferedImage image = VideoFrameUtils.getBufferedImage(data, width, data.length / width);

    final JLabel label = new JLabel();
    label.setIcon(new ImageIcon(image));

    final JFrame frame = new JFrame(name);
    frame.setSize(500, 300);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.add(label);
    frame.setVisible(true);

    this.saveToFile(image, name);
  }

  private void saveToFile(final BufferedImage image, final String name) {
    final Path dir = Paths.get(System.getProperty("user.dir"), "dither");
    try {
      FileUtils.createDirectoryIfNotExists(dir);
      ImageIO.write(image, "png", dir.resolve("%s.png".formatted(name)).toFile());
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
