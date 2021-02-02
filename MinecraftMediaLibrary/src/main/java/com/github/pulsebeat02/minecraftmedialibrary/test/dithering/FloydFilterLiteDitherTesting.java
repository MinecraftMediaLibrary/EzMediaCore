package com.github.pulsebeat02.minecraftmedialibrary.test.dithering;

import com.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.FilterLiteDither;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.FloydImageDither;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FloydFilterLiteDitherTesting extends JFrame {

    private static final long serialVersionUID = 2515798191284509909L;

    public FloydFilterLiteDitherTesting(@NotNull final File image) throws IOException {

        final BufferedImage before = ImageIO.read(image);

        final JPanel beforeDither = new JPanel();
        beforeDither.add(new JLabel(new ImageIcon(VideoUtilities.resizeImage(before, 500, 250))));
        beforeDither.add(new JLabel("Before Dithering"));
        beforeDither.add(new JLabel("| Width (before resize): " + before.getWidth()));
        beforeDither.add(new JLabel("| Height (before resize): " + before.getHeight()));

        final JPanel floydDithering = new JPanel();
        final long floydStart = System.currentTimeMillis();
        floydDithering.add(new JLabel(new ImageIcon(VideoUtilities.resizeImage(ditherFloyd(before), 500, 250))));
        final long floydEnd = System.currentTimeMillis();
        floydDithering.add(new JLabel("Floyd Steinberg Dithering"));
        floydDithering.add(new JLabel("| Time (Milliseconds): " + (floydEnd - floydStart)));

        final JPanel sierraDithering = new JPanel();
        final long sierraStart = System.currentTimeMillis();
        sierraDithering.add(new JLabel(new ImageIcon(VideoUtilities.resizeImage(ditherSierra(before), 500, 250))));
        final long sierraEnd = System.currentTimeMillis();
        sierraDithering.add(new JLabel("Sierra 2-4A Dithering"));
        sierraDithering.add(new JLabel("| Time (Milliseconds): " + (sierraEnd - sierraStart)));

        final Container container = getContentPane();
        container.setLayout(new GridLayout());
        container.add(beforeDither);
        container.add(floydDithering);
        container.add(sierraDithering);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                System.exit(0);
            }
        });

        pack();
        setVisible(true);

    }

    public static void main(final String[] args) throws IOException {
        // Windows: C:\\Users\\Brandon Li\\Desktop\\kingmammoth.png
        // Mac: /Users/bli24/Desktop/platform1/6vv2qz15h7e51.png
        new FloydFilterLiteDitherTesting(new File("/Users/bli24/Desktop/platform1/6vv2qz15h7e51.png"));
    }

    private BufferedImage ditherSierra(@NotNull final BufferedImage before) {
        final int[] buffer = VideoUtilities.getBuffer(before);
        new FilterLiteDither().dither(buffer, before.getWidth());
        return VideoUtilities.getBufferedImage(buffer, before.getWidth(), before.getHeight());
    }

    private BufferedImage ditherFloyd(@NotNull final BufferedImage before) {
        final int[] buffer = VideoUtilities.getBuffer(before);
        new FloydImageDither().dither(buffer, before.getWidth());
        return VideoUtilities.getBufferedImage(buffer, before.getWidth(), before.getHeight());
    }

}
