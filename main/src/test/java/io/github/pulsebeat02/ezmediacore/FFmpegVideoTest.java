package io.github.pulsebeat02.ezmediacore;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JFrame;
import org.jetbrains.annotations.NotNull;

public class FFmpegVideoTest {

  private final JFrame window;
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
                  this.window.add(new ImageFrame(img));
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
        if (frame == null) {
          return;
        }
        callback.accept(frame.getImage());
      }
    };
  }

  protected static class ImageFrame extends Component {

    @Serial
    private static final long serialVersionUID = 5417763373891009288L;

    private final BufferedImage image;

    public ImageFrame(final BufferedImage image) {
      this.image = image;
    }

    @Override
    public void paint(@NotNull final Graphics graphics) {
      graphics.drawImage(this.image, 0, 0, null);
    }
  }

}
