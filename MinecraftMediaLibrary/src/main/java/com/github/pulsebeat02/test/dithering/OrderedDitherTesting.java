package com.github.pulsebeat02.test.dithering;

import com.github.pulsebeat02.utility.VideoUtilities;
import com.github.pulsebeat02.video.dither.OrderedDithering;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class OrderedDitherTesting extends JFrame {

    public OrderedDitherTesting(@NotNull final File image) throws IOException {

        BufferedImage before = ImageIO.read(image);
        Container c = getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

        JPanel beforeDither = new JPanel();
        beforeDither.add(new JLabel(new ImageIcon(VideoUtilities.resizeImage(before, 500, 250))));
        beforeDither.add(new JLabel("Before Dithering"));
        beforeDither.add(new JLabel("| Width (before resize): " + before.getWidth()));
        beforeDither.add(new JLabel("| Height (before resize): " + before.getHeight()));
        c.add(beforeDither);

        for (OrderedDithering.DitherType type : OrderedDithering.DitherType.values()) {
            int[] buffer = VideoUtilities.getBuffer(before);
            long start = System.currentTimeMillis();
            new OrderedDithering(OrderedDithering.DitherType.ModeTwo).dither(buffer, before.getWidth());
            long end = System.currentTimeMillis();
            BufferedImage after = VideoUtilities.getBufferedImage(buffer, before.getWidth(), before.getHeight());
            JPanel panel = new JPanel();
            panel.add(new JLabel(new ImageIcon(VideoUtilities.resizeImage(after, 500, 350))));
            panel.add(new JLabel("Bayer Matrix (Ordered) Dithering (" + type.getName() + ")"));
            panel.add(new JLabel("| Time (Milliseconds): " + (end - start)));
            panel.setVisible(true);
            c.add(panel);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                System.exit(0);
            }
        });

        pack();

        setVisible(true);

    }


    public static void main(String[] args) throws IOException {
        // Windows: C:\\Users\\Brandon Li\\Desktop\\kingmammoth.png
        // Mac: /Users/bli24/Desktop/platform1/6vv2qz15h7e51.png
        // new FloydFilterLiteDitherTesting(new File("/Users/bli24/Desktop/platform1/6vv2qz15h7e51.png"));
        new OrderedDitherTesting(new File("C:\\Users\\Brandon Li\\Desktop\\0923e57e357298233d20cb38cccc8e7c.png"));
    }

}
