package com.github.pulsebeat02.utility;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

public class VideoUtilities {

    public static int[] getBuffer(final File image) {
        try {
            BufferedImage bi = ImageIO.read(image);
            return ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int[] getBuffer(final BufferedImage image) {
        return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

}
