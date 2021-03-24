/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/2/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Special video utilities used throughout the library and also open to users. Used for easier video
 * management.
 */
public final class VideoUtilities {

  /**
   * Get buffer from an image.
   *
   * @param image the image
   * @return the buffer
   */
  public static int[] getBuffer(@NotNull final File image) {
    try {
      return getBuffer(ImageIO.read(image));
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
  public static int[] getBuffer(@NotNull final BufferedImage image) {
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
  public static BufferedImage getBufferedImage(
      @NotNull final int[] rgb, final int width, final int height) {
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
  public static byte[] toByteArray(final int @NotNull [] array) {
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
  public static BufferedImage toBufferedImage(@NotNull final byte[] array) {
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
  public static BufferedImage resizeImage(
      @NotNull final BufferedImage image, final int width, final int height) {
    return resizeBufferedImage(
        image,
        getScaledDimension(
            new Dimension(image.getWidth(), image.getHeight()), new Dimension(width, height)));
  }
}
