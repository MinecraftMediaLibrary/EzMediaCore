package io.github.pulsebeat02.ezmediacore;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.jetbrains.annotations.NotNull;

public class FFmpegVideoTest {

  private final JFrame window;
  private FFmpeg ffmpeg;

  public FFmpegVideoTest(@NotNull final String binary, @NotNull final String input)
      throws IOException {
    this.window = new JFrame("Example Video");
    this.window.setSize(1024, 2048);
    this.window.setVisible(true);
    this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.init(Path.of(binary), Path.of(input), 0L);
    this.ffmpeg.execute();
  }

  public static void main(final String[] args) throws IOException {
    new FFmpegVideoTest(
        "C:\\Users\\Brandon Li\\Desktop\\server\\plugins\\DeluxeMediaPlugin\\emc\\libs\\ffmpeg\\ffmpeg-amd64.exe",
        "C:\\Users\\Brandon Li\\Desktop\\video.mp4");
  }

  private void init(@NotNull final Path path, @NotNull final Path input, final long ms)
      throws IOException {
    final int delay = (int) (1000 / VideoFrameUtils.getFrameRate(path, input).orElse(30));
    this.ffmpeg =
        new FFmpeg(path)
            .addInput(UrlInput.fromPath(input).setPosition(ms))
            .addOutput(
                FrameOutput.withConsumer(
                    this.getFrameConsumer(
                        img -> {
                          if (img == null) {
                            return;
                          }
                          this.window.getContentPane().removeAll();
                          this.window.add(new JLabel("", new ImageIcon(img), JLabel.CENTER));
                          this.window.repaint();
                          this.window.revalidate();
                          try {
                            TimeUnit.MILLISECONDS.sleep(delay);
                          } catch (final InterruptedException e) {
                            e.printStackTrace();
                          }
                        })));
  }

  private FrameConsumer getFrameConsumer(final Consumer<BufferedImage> callback) {
    return new FrameConsumer() {
      @Override
      public void consumeStreams(final List<Stream> streams) {
      }

      @Override
      public void consume(final Frame frame) {
        if (frame == null) {
          return;
        }
        callback.accept(frame.getImage());
      }
    };
  }
}
