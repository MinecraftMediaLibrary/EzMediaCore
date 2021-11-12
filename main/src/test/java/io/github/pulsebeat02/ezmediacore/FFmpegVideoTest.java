package io.github.pulsebeat02.ezmediacore;

import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class FFmpegVideoTest {

  private final ArrayBlockingQueue<BufferedImage> frames;
  private final JFrame window;
  private final AtomicBoolean running;
  private FFmpeg ffmpeg;

  public FFmpegVideoTest(@NotNull final String binary, @NotNull final String input) {
    this.frames = new ArrayBlockingQueue<>(30);
    this.running = new AtomicBoolean(true);
    final Path bin = Path.of(binary);
    final Path in = Path.of(input);
    this.window = new JFrame("Example Video");
    this.window.setSize(1024, 2048);
    this.window.setVisible(true);
    this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.window.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(@NotNull final WindowEvent windowEvent) {
        FFmpegVideoTest.this.running.set(false);
      }
    });
    this.init(bin, in, 0L);
    this.ffmpeg.execute();
  }

  public static void main(final String[] args) throws IOException {
    new FFmpegVideoTest("/Users/bli24/Desktop/ffmpeg/ffmpeg-x86_64-osx",
        "/Users/bli24/Downloads/test.mp4");
  }

  private void init(@NotNull final Path path, @NotNull final Path input, final long ms) {
    this.ffmpeg =
        new FFmpeg(path)
            .addInput(UrlInput.fromPath(input).setPosition(ms).addArgument("-re"))
            .addOutput(FrameOutput.withConsumer(this.getFrameConsumer()))
            .setLogLevel(LogLevel.FATAL)
            .setProgressListener(line -> {
            })
            .setOutputListener(line -> {
            });
    this.displayThread();
  }

  @Contract(value = " -> new", pure = true)
  private @NotNull FrameConsumer getFrameConsumer() {
    return new FrameConsumer() {

      @Override
      public void consumeStreams(final List<Stream> streams) {
      }

      @Override
      public void consume(final Frame frame) {

        // sometimes ffmpeg returns a null frame...
        if (frame == null) {
          return;
        }

        // sometimes it is an audio frame (or non-video frame in general). We don't want audio
        // frames
        final BufferedImage image = frame.getImage();
        if (image == null) {
          return;
        }

        // hack to wait for new frames
        while (FFmpegVideoTest.this.frames.remainingCapacity() <= 1) {
          try {
            TimeUnit.MILLISECONDS.sleep(5);
          } catch (final InterruptedException e) {
            e.printStackTrace();
          }
        }

        // add to queue
        FFmpegVideoTest.this.frames.add(image);
      }
    };
  }

  public void displayThread() {
    CompletableFuture.runAsync(
        () -> {
          while (this.running.get()) {
            try {
              final BufferedImage image = this.frames.take();
              this.window.getContentPane().removeAll();
              this.window.add(new JLabel("", new ImageIcon(image), JLabel.CENTER));
              this.window.repaint();
              this.window.revalidate();
            } catch (final InterruptedException e) {
              e.printStackTrace();
            }
          }
        });
  }
}