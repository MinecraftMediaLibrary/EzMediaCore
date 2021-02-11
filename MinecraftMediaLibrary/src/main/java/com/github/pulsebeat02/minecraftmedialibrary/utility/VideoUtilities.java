/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
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

public class VideoUtilities {

  public static int[] getBuffer(@NotNull final File image) {
    try {
      return getBuffer(ImageIO.read(image));
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static int[] getBuffer(@NotNull final BufferedImage image) {
    return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
  }

  public static BufferedImage getBufferedImage(
      @NotNull final int[] rgb, final int width, final int height) {
    final BufferedImage image = new BufferedImage(width, height, 1);
    image.setRGB(0, 0, width, height, rgb, 0, width);
    return image;
  }

  public static byte[] toByteArray(@NotNull final int[] array) {
    final ByteBuffer buffer = ByteBuffer.allocate(array.length * 4);
    final IntBuffer intBuffer = buffer.asIntBuffer();
    intBuffer.put(array);
    return buffer.array();
  }

  public static BufferedImage toBufferedImage(@NotNull final byte[] array) {
    final ByteArrayInputStream bis = new ByteArrayInputStream(array);
    try {
      return ImageIO.read(bis);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return null;
  }

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

  public static Dimension getScaledDimension(
      @NotNull final Dimension imgSize, @NotNull final Dimension boundary) {
    final int original_width = imgSize.width;
    final int original_height = imgSize.height;
    final int bound_width = boundary.width;
    final int bound_height = boundary.height;
    int new_width = original_width;
    int new_height = original_height;
    if (original_width > bound_width) {
      new_width = bound_width;
      new_height = (new_width * original_height) / original_width;
    }
    if (new_height > bound_height) {
      new_height = bound_height;
      new_width = (new_height * original_width) / original_height;
    }
    return new Dimension(new_width, new_height);
  }

  public static BufferedImage resizeImage(
      @NotNull final BufferedImage image, final int width, final int height) {
    return resizeBufferedImage(
        image,
        getScaledDimension(
            new Dimension(image.getWidth(), image.getHeight()), new Dimension(width, height)));
  }
}
