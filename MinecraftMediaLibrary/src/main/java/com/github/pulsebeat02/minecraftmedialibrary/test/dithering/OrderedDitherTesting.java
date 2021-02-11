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

package com.github.pulsebeat02.minecraftmedialibrary.test.dithering;

import com.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.OrderedDithering;
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

  private static final long serialVersionUID = -47246908626887986L;

  public OrderedDitherTesting(@NotNull final File image) throws IOException {

    final BufferedImage before = ImageIO.read(image);
    final Container c = getContentPane();
    c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

    final JPanel beforeDither = new JPanel();
    beforeDither.add(new JLabel(new ImageIcon(VideoUtilities.resizeImage(before, 500, 250))));
    beforeDither.add(new JLabel("Before Dithering"));
    beforeDither.add(new JLabel("| Width (before resize): " + before.getWidth()));
    beforeDither.add(new JLabel("| Height (before resize): " + before.getHeight()));
    c.add(beforeDither);

    for (final OrderedDithering.DitherType type : OrderedDithering.DitherType.values()) {
      final int[] buffer = VideoUtilities.getBuffer(before);
      final long start = System.currentTimeMillis();
      new OrderedDithering(OrderedDithering.DitherType.ModeTwo).dither(buffer, before.getWidth());
      final long end = System.currentTimeMillis();
      final BufferedImage after =
          VideoUtilities.getBufferedImage(buffer, before.getWidth(), before.getHeight());
      final JPanel panel = new JPanel();
      panel.add(new JLabel(new ImageIcon(VideoUtilities.resizeImage(after, 300, 250))));
      panel.add(new JLabel("Bayer Matrix (Ordered) Dithering (" + type.getName() + ")"));
      panel.add(new JLabel("| Time (Milliseconds): " + (end - start)));
      panel.setVisible(true);
      c.add(panel);
    }

    addWindowListener(
        new WindowAdapter() {
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
    new OrderedDitherTesting(new File("/Users/bli24/Desktop/platform1/6vv2qz15h7e51.png"));
    // new OrderedDitherTesting(new File("C:\\Users\\Brandon
    // Li\\Desktop\\0923e57e357298233d20cb38cccc8e7c.png"));
  }
}
