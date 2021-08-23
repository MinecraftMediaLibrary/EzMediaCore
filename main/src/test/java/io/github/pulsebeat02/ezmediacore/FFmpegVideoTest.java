package io.github.pulsebeat02.ezmediacore;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.jetbrains.annotations.NotNull;

public class FFmpegVideoTest {

  private final JFrame window;
  private JLabel label;
  private FFmpeg ffmpeg;

  public FFmpegVideoTest(@NotNull final String binary, @NotNull final String input)
      throws IOException {
    this.window = new JFrame("Example Video");
    this.window.setSize(1024, 2048);
    this.window.setVisible(true);
    this.init(Path.of(binary), Path.of(input), 0L);
    this.ffmpeg.execute();
  }

  public static void main(final String[] args) throws IOException {
    new FFmpegVideoTest(
        "C:\\Users\\Brandon Li\\Desktop\\server\\plugins\\DeluxeMediaPlugin\\emc\\libs\\ffmpeg\\ffmpeg-amd64.exe",
        "C:\\Users\\Brandon Li\\Desktop\\video.mp4");
  }

  private void init(@NotNull final Path path, @NotNull final Path input, final long ms) {
    this.ffmpeg = new FFmpeg(path)
        .addInput(
            UrlInput.fromPath(input).setPosition(ms))
        .addOutput(
            FrameOutput.withConsumer(
                this.getFrameConsumer(img -> {
                  this.window.getContentPane().removeAll();
                  this.window.add(new JLabel("", new ImageIcon(img), JLabel.CENTER));
                  this.window.repaint();
                  this.window.revalidate();
                })));
  }

  private FrameConsumer getFrameConsumer(
      final Consumer<BufferedImage> callback) {
    return new FrameConsumer() {
      @Override
      public void consumeStreams(final List<Stream> streams) {
      }

      @Override
      public void consume(final Frame frame) {
        callback.accept(frame.getImage());
      }
    };
  }

//  protected class ImageFrame {
//
//    @Serial
//    private static final long serialVersionUID = 5417763373891009288L;
//
//    private final BufferedImage image;
//
//    public ImageFrame(final BufferedImage image) {
//      this.image = image;
//    }
//
//    @Override
//    public void paint(@NotNull final Graphics graphics) {
//      final Container container = FFmpegVideoTest.this.window.getContentPane();
//      container.removeAll();
//      container.add(new )
//
//      graphics.drawImage(this.image, 0, 0, null);
//    }
//  }

}
