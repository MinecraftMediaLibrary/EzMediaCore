package com.github.pulsebeat02.utility;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int[] getBuffer(@NotNull final BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    public static BufferedImage getBufferedImage(@NotNull final int[] rgb, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, 1);
        image.setRGB(0, 0, width, height, rgb, 0, width);
        return image;
    }

    public static byte[] toByteArray(@NotNull final int[] array) {
        ByteBuffer buffer = ByteBuffer.allocate(array.length * 4);
        IntBuffer intBuffer = buffer.asIntBuffer();
        intBuffer.put(array);
        return buffer.array();
    }

    public static BufferedImage toBufferedImage(@NotNull final byte[] array) {
        ByteArrayInputStream bis = new ByteArrayInputStream(array);
        try {
            return ImageIO.read(bis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static BufferedImage resizeBufferedImage(@NotNull final BufferedImage originalImage,
                                                     final Dimension dim) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage resizedImage = new BufferedImage(dim.width, dim.height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(originalImage, 0, 0, dim.width, dim.height, null);
        g.dispose();
        return resizedImage;
    }

    public static Dimension getScaledDimension(@NotNull final Dimension imgSize,
                                               @NotNull final Dimension boundary) {
        int original_width = imgSize.width;
        int original_height = imgSize.height;
        int bound_width = boundary.width;
        int bound_height = boundary.height;
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

    public static BufferedImage resizeImage(@NotNull final BufferedImage image,
                                            final int width, final int height) {
        return resizeBufferedImage(image, getScaledDimension(new Dimension(image.getWidth(), image.getHeight()), new Dimension(width, height)));
    }



}
