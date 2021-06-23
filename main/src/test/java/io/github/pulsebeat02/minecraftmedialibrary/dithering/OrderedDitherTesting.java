/*.........................................................................................
. Copyright © 2021 Brandon Li
.                                                                                        .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this
. software and associated documentation files (the “Software”), to deal in the Software
. without restriction, including without limitation the rights to use, copy, modify, merge,
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit
. persons to whom the Software is furnished to do so, subject to the following conditions:
.
. The above copyright notice and this permission notice shall be included in all copies
. or substantial portions of the Software.
.
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
. EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
. MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
. NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
. BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
. ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
. CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
.  SOFTWARE.
.                                                                                        .
.........................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.dithering;

import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.OrderedDithering;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OrderedDitherTesting extends JFrame {

  private static final long serialVersionUID = -3834289624442490328L;

  public OrderedDitherTesting(final File image) throws IOException {
    final BufferedImage before = ImageIO.read(image);
    final Container c = getContentPane();
    c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
    final JPanel beforeDither = new JPanel();
    beforeDither.add(new JLabel(new ImageIcon(VideoUtilities.resizeImage(before, 500, 250))));
    beforeDither.add(new JLabel("Before Dithering"));
    beforeDither.add(new JLabel("| Width (before resize): " + before.getWidth()));
    beforeDither.add(new JLabel("| Height (before resize): " + before.getHeight()));
    c.add(beforeDither);
    for (final OrderedDithering.DitherType value : OrderedDithering.DitherType.values()) {
      final int[] buffer = VideoUtilities.getBuffer(before);
      final long start = System.currentTimeMillis();
      new OrderedDithering(value).dither(buffer, before.getWidth());
      final long end = System.currentTimeMillis();
      final BufferedImage after =
          VideoUtilities.getBufferedImage(buffer, before.getWidth(), before.getHeight());
      final JPanel panel = new JPanel();
      panel.add(new JLabel(new ImageIcon(VideoUtilities.resizeImage(after, 300, 250))));
      panel.add(new JLabel("Bayer Matrix (Ordered) Dithering (" + value.getName() + ")"));
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
    new OrderedDitherTesting(new File("C:\\Users\\Brandon Li\\Desktop\\kingmammoth.png"));
  }
}
