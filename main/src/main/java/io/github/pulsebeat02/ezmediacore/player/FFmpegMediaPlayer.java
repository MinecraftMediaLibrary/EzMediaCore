package io.github.pulsebeat02.ezmediacore.player;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResultFuture;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.FrameCallback;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;

public class FFmpegMediaPlayer extends MediaPlayer {

  private final Queue<int[]> frames;
  private final long delay;

  private FFmpeg ffmpeg;
  private FFmpegResultFuture future;
  private int[] previous;
  private long start;
  private boolean audio;

  FFmpegMediaPlayer(
      @NotNull final FrameCallback callback,
      @NotNull final Dimension pixelDimension,
      @NotNull final String url,
      @Nullable final String key,
      final int fps) {
    super(callback, pixelDimension, url, key, fps);
    this.frames = new ArrayBlockingQueue<>(fps);
    this.initializePlayer(0L);
    long deferred = 30;
    try {
      deferred = 1000 / VideoFrameUtils.getFrameRate(this.getCallback().getCore(), Path.of(url)).orElse(30);
    } catch (final IOException e) {
      Logger.error(
              "A severe error occurred with extracting the fps of a video! Resorting to 30 fps!");
      e.printStackTrace();
    }
    this.delay = deferred;
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls) {
    super.setPlayerState(controls);
    switch (controls) {
      case START -> {
        if (this.ffmpeg == null) {
          this.initializePlayer(0L);
        }
        this.play();
        this.start = System.currentTimeMillis();
      }
      case PAUSE -> {
        if (this.ffmpeg != null) {
          this.future.graceStop();
        }
        this.stopAudio();
        this.start = System.currentTimeMillis();
      }
      case RESUME -> {
        this.initializePlayer(System.currentTimeMillis() - this.start);
        this.play();
      }
      case RELEASE -> {
        if (this.ffmpeg != null) {
          this.future.graceStop();
          this.future = null;
        }
      }
      default -> throw new IllegalArgumentException("Player state is invalid!");
    }
  }

  @Override
  public void initializePlayer(final long seconds) {

    final Dimension dimension = this.getDimensions();
    final String url = this.getUrl();
    final Path path = Path.of(url);
    final long ms = seconds * 1000;

    this.ffmpeg =
        new FFmpeg(this.getCore().getFFmpegPath())
            .addInput(
                Files.exists(path)
                    ? UrlInput.fromPath(path).setPosition(ms)
                    : UrlInput.fromUrl(url).setPosition(ms))
            .addOutput(
                FrameOutput.withConsumer(this.getFrameConsumer())
                    .setFrameRate(this.getFrameRate())
                    .disableStream(StreamType.AUDIO)
                    .disableStream(StreamType.SUBTITLE)
                    .disableStream(StreamType.DATA)
                    .setFrameRate(getFrameRate()))
            .addArguments("-vf",
                "scale=%s:%s".formatted(dimension.getWidth(), dimension.getHeight()));
  }

  @Override
  public long getElapsedMilliseconds() {
    return System.currentTimeMillis() - this.start;
  }

  private FrameConsumer getFrameConsumer() {
    return new FrameConsumer() {
      @Override
      public void consumeStreams(final List<Stream> streams) {
      }

      @Override
      public void consume(final Frame frame) {

        if (frame == null) {
          return;
        }

        final BufferedImage image = frame.getImage();
        if (image == null) {
          return;
        }

        previous = VideoFrameUtils.getRGBParallel(image);

        while (frames.size() <= 1) {
          try {
            Thread.sleep(5);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          frames.add(previous);
        }

        if (FFmpegMediaPlayer.this.audio) {
          FFmpegMediaPlayer.this.playAudio();
          FFmpegMediaPlayer.this.audio = false;
        }
      }
    };
  }

  private void play() {
    this.future = this.ffmpeg.executeAsync(ExecutorProvider.FFMPEG_VIDEO_PLAYER);
    this.audio = true;
    final Callback callback = this.getCallback();
    CompletableFuture.runAsync(() -> {
      while (!future.isDone()) {
        callback.process(frames.poll());
        try {
          Thread.sleep(delay);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }, ExecutorProvider.FFMPEG_VIDEO_PLAYER);
  }


}
