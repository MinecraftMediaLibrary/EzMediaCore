package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ImageTest {

  public static void main(final String[] args) throws IOException {
    final BufferedImage image =
        ImageIO.read(
            new URL(
                "https://image.shutterstock.com/image-photo/word-example-written-on-magnifying-260nw-1883859943.jpg"));
    final int[] rgb = VideoFrameUtils.getRGBParallel(image);
    final int[] original =
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());

    final BufferedImage after =
        VideoFrameUtils.getBufferedImage(rgb, image.getWidth(), image.getHeight());
    final JFrame frame = new JFrame();
    final Container container = frame.getContentPane();
    container.setLayout(new FlowLayout());
    container.add(new JLabel(new ImageIcon(after)));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);

    //    System.out.println(Arrays.toString(rgb));
    //    for (int i = 0; i < rgb.length; i++) {
    //      final int color = rgb[i];
    //      final int old = original[i];
    //      if (color != old) {
    //        System.out.printf("Index: %d mismatch [%d,%d]%n", i, color, old);
    //      }
    //    }
  }
}
