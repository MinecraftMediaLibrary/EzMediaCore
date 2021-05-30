/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.utility;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Special video utilities used throughout the library and also open to users. Used for easier video
 * management.
 */
public final class VideoUtilities {

  private VideoUtilities() {}

  /**
   * Get buffer from an image.
   *
   * @param image the image
   * @return the buffer
   */
  public static int @Nullable [] getBuffer(@NotNull final Path image) {
    try {
      return getBuffer(ImageIO.read(image.toFile()));
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Gets buffer from a BufferedImage.
   *
   * @param image the image
   * @return the buffer
   */
  public static int @NotNull [] getBuffer(@NotNull final BufferedImage image) {
    return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
  }

  /**
   * Gets BufferedImage from buffer.
   *
   * @param rgb the rgb
   * @param width the width
   * @param height the height
   * @return resulting BufferedImage
   */
  @NotNull
  public static BufferedImage getBufferedImage(
      final int @NotNull [] rgb, final int width, final int height) {
    final BufferedImage image = new BufferedImage(width, height, 1);
    image.setRGB(0, 0, width, height, rgb, 0, width);
    return image;
  }

  /**
   * Converts an integer array to a byte array.
   *
   * @param array the array
   * @return resulting byte[]
   */
  public static byte @NotNull [] toByteArray(final int @NotNull [] array) {
    final ByteBuffer buffer = ByteBuffer.allocate(array.length * 4);
    final IntBuffer intBuffer = buffer.asIntBuffer();
    intBuffer.put(array);
    return buffer.array();
  }

  /**
   * Convert a byte[] image data to BufferedImage
   *
   * @param array the array
   * @return resulting BufferedImage
   */
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

  /**
   * Resizes BufferedImage to dimensions.
   *
   * @param originalImage image to resize
   * @param dim dimension
   * @return resulting BufferedImage
   */
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

  /**
   * Gets Scaled Dimension.
   *
   * @param imgSize the img size
   * @param boundary the boundary
   * @return resulting scaled dimension
   */
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

  /**
   * Resize Buffered Image.
   *
   * @param image the image
   * @param width the width
   * @param height the height
   * @return resulting rescaled BufferedImage
   */
  @NotNull
  public static BufferedImage resizeImage(
      @NotNull final BufferedImage image, final int width, final int height) {
    return resizeBufferedImage(
        image,
        getScaledDimension(
            new Dimension(image.getWidth(), image.getHeight()), new Dimension(width, height)));
  }

  /**
   * Gets the dimensions of an image file.
   *
   * @param file the image file
   * @return the dimensions of the image
   * @throws IOException if an issue occurred while fetching the image data
   */
  public static Dimension getDimensions(@NotNull final Path file) throws IOException {
    final Iterator<ImageReader> iter =
        ImageIO.getImageReadersBySuffix(FilenameUtils.getExtension(PathUtilities.getName(file)));
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
}
