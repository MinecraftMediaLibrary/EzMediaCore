package io.github.pulsebeat02.ezmediacore;

import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.OutputListener;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import io.github.pulsebeat02.ezmediacore.player.FFmpegMediaPlayer;
import io.github.pulsebeat02.ezmediacore.throwable.InvalidStreamHeaderException;
import io.github.pulsebeat02.ezmediacore.utility.Pair;
import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class FFmpegVideoTest {

  private final JFrame window;
  private final ArrayBlockingQueue<Pair<BufferedImage, Long>> frames;
  private final long startEpoch;
  private final int delay;
  private FFmpeg ffmpeg;

  public FFmpegVideoTest(@NotNull final String binary, @NotNull final String input)
      throws IOException {
    this.startEpoch = Instant.now().toEpochMilli();
    final Path bin = Path.of(binary);
    final Path in = Path.of(input);
    this.delay = (int) (1000 / VideoFrameUtils.getFrameRate(bin, in).orElse(30));
    this.window = new JFrame("Example Video");
    this.window.setSize(1024, 2048);
    this.window.setVisible(true);
    this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.frames = new ArrayBlockingQueue<>(50);
    this.init(bin, in, 0L);
    this.ffmpeg.execute();
  }

  public static void main(final String[] args) throws IOException {
    new FFmpegVideoTest("/Users/bli24/Desktop/ffmpeg/ffmpeg-x84_64-osx", "");
  }

  private void init(@NotNull final Path path, @NotNull final Path input, final long ms)
      throws IOException {
    this.ffmpeg =
        new FFmpeg(path)
            .addInput(UrlInput.fromPath(input).setPosition(ms))
            .addOutput(FrameOutput.withConsumer(this.getFrameConsumer(img -> {})))
            .setLogLevel(LogLevel.FATAL)
            .setProgressListener(line -> {})
            .setOutputListener(line -> {});
  }

  @Contract(value = "_ -> new", pure = true)
  private @NotNull FrameConsumer getFrameConsumer(final Consumer<BufferedImage> callback) {
    return new FrameConsumer() {

      private Stream[] streams;

      @Override
      public void consumeStreams(final List<Stream> streams) {
        // if stream ids are not properly ordered, sometimes we need to rearrange them
        final int max =
            streams.stream()
                .mapToInt(Stream::getId)
                .max()
                .orElseThrow(InvalidStreamHeaderException::new);

        // create our lookup table
        this.streams = new Stream[max];

        // loop through elements, set id with proper stream
        for (final Stream stream : streams) {
          this.streams[stream.getId()] = stream;
        }
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

        // hack to wait for the player for frames.
        while (frames.remainingCapacity() <= 1) {
          try {
            TimeUnit.MILLISECONDS.sleep(5);
          } catch (final InterruptedException e) {
            e.printStackTrace();
          }
        }

        // add to queue
        frames.add(
            Pair.ofPair(
                image,
                (long)
                    ((frame.getPts() * (1.0F / this.streams[frame.getStreamId()].getTimebase()))
                        * 1000)));
      }
    };
  }

  public void displayThread() {
    CompletableFuture.runAsync(
        () -> {
          while (!frames.isEmpty()) {
            final Pair<BufferedImage, Long> frame = frames.poll();
            this.window.getContentPane().removeAll();
            this.window.add(new JLabel("", new ImageIcon(frame.getKey()), JLabel.CENTER));
            this.window.repaint();
            this.window.revalidate();
            try {
              TimeUnit.MILLISECONDS.sleep(delay);
            } catch (final InterruptedException e) {
              e.printStackTrace();
            }
            final long passed = Instant.now().toEpochMilli() - this.startEpoch;
            Pair<BufferedImage, Long> skip = this.frames.peek();
            while (skip.getValue() <= passed) {
              skip = this.frames.poll();
            }
          }
        });
  }
}
