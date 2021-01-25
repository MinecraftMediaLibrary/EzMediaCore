package com.github.pulsebeat02.utility;

import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
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
        return image.getRGB(0,0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
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

}
