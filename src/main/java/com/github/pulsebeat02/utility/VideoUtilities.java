package com.github.pulsebeat02.utility;

import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

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
        BufferedImage cast = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        return ((DataBufferInt) cast.getRaster().getDataBuffer()).getData();
    }

    public static byte[] byteBufferArrayTranslation(@NotNull final ByteBuffer buffer) {
        byte[] arr = new byte[buffer.remaining()];
        buffer.get(arr);
        return arr;
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
