package io.github.pulsebeat02.epicmedialib.utility;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
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
import org.jetbrains.annotations.Nullable;

public final class VideoFrameUtils {

  private static final RgbToBgr CONVERSION;

  static {
    CONVERSION = new RgbToBgr();
  }

  private VideoFrameUtils() {}

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

  @Nullable
  public static BufferedImage toBufferedImage(final byte @NotNull [] array) {
    final ByteArrayInputStream bis = new ByteArrayInputStream(array);
    try {
      return ImageIO.read(bis);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @NotNull
  private static BufferedImage resizeBufferedImage(
      @NotNull final BufferedImage originalImage, final Dimension dim) {
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

  public static BufferedImage toBufferedImage(Picture src) {
    if (src.getColor() != ColorSpace.BGR) {
      final Picture bgr =
          Picture.createCropped(src.getWidth(), src.getHeight(), ColorSpace.BGR, src.getCrop());
      if (src.getColor() == ColorSpace.RGB) {
        CONVERSION.transform(src, bgr);
      } else {
        final Transform transform = ColorUtil.getTransform(src.getColor(), ColorSpace.RGB);
        transform.transform(src, bgr);
        CONVERSION.transform(bgr, bgr);
      }
      src = bgr;
    }
    final BufferedImage dst =
        new BufferedImage(
            src.getCroppedWidth(), src.getCroppedHeight(), BufferedImage.TYPE_3BYTE_BGR);
    if (src.getCrop() == null) {
      toBufferedImage(src, dst);
    } else {
      toCroppedBufferedImage(src, dst);
    }
    return dst;
  }

  public static void toBufferedImage(final Picture src, final BufferedImage dst) {
    final byte[] data = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
    final byte[] srcData = src.getPlaneData(0);
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) (srcData[i] + 128);
    }
  }

  public static void toCroppedBufferedImage(final Picture src, final BufferedImage dst) {
    final byte[] data = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
    final byte[] srcData = src.getPlaneData(0);
    final int dstStride = dst.getWidth() * 3;
    final int srcStride = src.getWidth() * 3;
    for (int line = 0, srcOff = 0, dstOff = 0; line < dst.getHeight(); line++) {
      for (int id = dstOff, is = srcOff; id < dstOff + dstStride; id += 3, is += 3) {
        data[id] = (byte) (srcData[is] + 128);
        data[id + 1] = (byte) (srcData[is + 1] + 128);
        data[id + 2] = (byte) (srcData[is + 2] + 128);
      }
      srcOff += srcStride;
      dstOff += dstStride;
    }
  }
}
