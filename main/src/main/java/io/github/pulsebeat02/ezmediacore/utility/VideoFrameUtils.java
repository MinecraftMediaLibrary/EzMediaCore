/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.utility;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.io.FilenameUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.RgbToBgr;
import org.jcodec.scale.Transform;
import org.jetbrains.annotations.NotNull;

public final class VideoFrameUtils {

  private VideoFrameUtils() {
  }

  public static int @NotNull [] toResizedColorArray(
      @NotNull final Picture frame,
      @NotNull final io.github.pulsebeat02.ezmediacore.dimension.Dimension dimension) {
    return getRGBParallel(
        resizeImage(toBufferedImage(frame), dimension.getWidth(), dimension.getHeight()));
  }

  public static @NotNull BufferedImage toBufferedImage(@NotNull Picture src) {
    final ColorSpace space = src.getColor();
    if (space != ColorSpace.BGR) {
      final Picture bgr =
          Picture.createCropped(src.getWidth(), src.getHeight(), ColorSpace.BGR, src.getCrop());
      if (space == ColorSpace.RGB) {
        new RgbToBgr().transform(src, bgr);
      } else {
        final Transform transform = ColorUtil.getTransform(space, ColorSpace.RGB);
        transform.transform(src, bgr);
        new RgbToBgr().transform(bgr, bgr);
      }
      src = bgr;
    }
    final BufferedImage dst =
        new BufferedImage(
            src.getCroppedWidth(), src.getCroppedHeight(), BufferedImage.TYPE_3BYTE_BGR);
    if (src.getCrop() == null) {
      toBufferedImage(src, dst);
    } else {
      toBufferedImageCropped(src, dst);
    }
    return dst;
  }

  public static void toBufferedImage(@NotNull final Picture src, @NotNull final BufferedImage dst) {
    final byte[] data = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
    final byte[] srcData = src.getPlaneData(0);
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) (srcData[i] + 128);
    }
  }

  private static void toBufferedImageCropped(
      @NotNull final Picture src, @NotNull final BufferedImage dst) {
    final byte[] data = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
    final byte[] srcData = src.getPlaneData(0);
    final int dstWidth = dst.getWidth();
    final int srcWidth = src.getWidth();
    final int dstStride = (dstWidth << 1) + dstWidth;
    final int srcStride = (srcWidth << 1) + srcWidth;
    for (int line = 0, srcOff = 0, dstOff = 0; line < dst.getHeight(); line++) {
      for (int id = dstOff, is = srcOff; id < dstOff + dstStride; id++, is++) {
        data[id] = (byte) (srcData[is] + 128);
        data[id++] = (byte) (srcData[is++] + 128);
        data[id++] = (byte) (srcData[is++] + 128);
      }
      srcOff += srcStride;
      dstOff += dstStride;
    }
  }

  @NotNull
  public static Optional<int[]> getBuffer(@NotNull final Path image) {
    try {
      return Optional.of(getBuffer(ImageIO.read(image.toFile())));
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public static int @NotNull [] getBuffer(@NotNull final BufferedImage image) {
    return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
  }

  @NotNull
  public static BufferedImage getBufferedImage(
      final int @NotNull [] rgb, final int width, final int height) {
    final BufferedImage image = new BufferedImage(width, height, 1);
    image.setRGB(0, 0, width, height, rgb, 0, width);
    return image;
  }

  public static byte @NotNull [] toByteArray(final int @NotNull [] array) {
    final ByteBuffer buffer = ByteBuffer.allocate(array.length * 4);
    final IntBuffer intBuffer = buffer.asIntBuffer();
    intBuffer.put(array);
    return buffer.array();
  }

  public static @NotNull BufferedImage toBufferedImage(
      final byte @NotNull [] array, final int width, final int height) {
    final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    final byte[] arr = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    System.arraycopy(array, 0, arr, 0, arr.length);
    return image;
  }

  @NotNull
  private static BufferedImage resizeBufferedImage(
      @NotNull final BufferedImage originalImage, @NotNull final Dimension dim) {
    final int type = BufferedImage.TYPE_INT_ARGB;
    final BufferedImage resizedImage = new BufferedImage(dim.width, dim.height, type);
    final Graphics2D g = resizedImage.createGraphics();
    g.setComposite(AlphaComposite.Src);
    g.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.drawImage(originalImage, 0, 0, dim.width, dim.height, null);
    g.dispose();
    return resizedImage;
  }

  @NotNull
  public static Dimension getScaledDimension(
      @NotNull final Dimension imgSize, @NotNull final Dimension boundary) {
    final int origWidth = imgSize.width;
    final int origHeight = imgSize.height;
    final int boundWidth = boundary.width;
    final int boundHeight = boundary.height;
    int newWidth = origWidth;
    int newHeight = origHeight;
    if (origWidth > boundWidth) {
      newWidth = boundWidth;
      newHeight = (newWidth * origHeight) / origWidth;
    }
    if (newHeight > boundHeight) {
      newHeight = boundHeight;
      newWidth = (newHeight * origWidth) / origHeight;
    }
    return new Dimension(newWidth, newHeight);
  }

  @NotNull
  public static BufferedImage resizeImage(
      @NotNull final BufferedImage image, final int width, final int height) {
    return resizeBufferedImage(
        image,
        getScaledDimension(
            new Dimension(image.getWidth(), image.getHeight()), new Dimension(width, height)));
  }

  @NotNull
  public static Dimension getDimensions(@NotNull final Path file) throws IOException {
    final Iterator<ImageReader> iter =
        ImageIO.getImageReadersBySuffix(FilenameUtils.getExtension(PathUtils.getName(file)));
    while (iter.hasNext()) {
      final ImageReader reader = iter.next();
      try {
        final ImageInputStream stream = new FileImageInputStream(file.toFile());
        reader.setInput(stream);
        final int width = reader.getWidth(reader.getMinIndex());
        final int height = reader.getHeight(reader.getMinIndex());
        return new Dimension(width, height);
      } catch (final IOException e) {
        e.printStackTrace();
      } finally {
        reader.dispose();
      }
    }
    throw new IOException("Not a known image file: " + file.toAbsolutePath());
  }

  public static OptionalDouble getFrameRate(
      @NotNull final MediaLibraryCore core, @NotNull final Path video) throws IOException {
    return getFrameRate(core.getFFmpegPath(), video);
  }

  public static OptionalDouble getFrameRate(@NotNull final Path binary, @NotNull final Path video)
      throws IOException {
    try (final BufferedReader r =
        new BufferedReader(
            new InputStreamReader(
                new ProcessBuilder(binary.toString(), "-i", video.toString())
                    .start()
                    .getErrorStream()))) { // ffmpeg always thinks its an error
      String line;
      while (true) {
        line = r.readLine();
        if (line == null) {
          break;
        }
        if (line.contains(" fps")) {
          final int fpsIndex = line.indexOf(" fps");
          return OptionalDouble.of(
              Double.parseDouble(
                  line.substring(line.lastIndexOf(",", fpsIndex) + 1, fpsIndex).trim()));
        }
      }
    }
    return OptionalDouble.empty();
  }

  public static int @NotNull [] getRGBParallel(@NotNull final BufferedImage image) {
    final int width = image.getWidth();
    final int height = image.getHeight();
    final int[] rgb = new int[width * height];
    final int num = width >> 5;
    IntStream.range(0, num + ((width & 31) == 0 ? 0 : 1))
        .parallel()
        .forEach(
            chunk -> {
              final int pixel = chunk << 5;
              if (chunk == num) {
                image.getRGB(pixel, 0, width - (num << 5), height, rgb, pixel, width);
              } else {
                image.getRGB(pixel, 0, 32, height, rgb, pixel, width);
              }
            });
    return rgb;
  }
}
