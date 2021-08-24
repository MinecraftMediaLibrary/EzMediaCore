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
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FFmpegMediaPlayer extends MediaPlayer {

  private FFmpeg ffmpeg;
  private FFmpegResultFuture future;
  private long start;

  private boolean audio;

  FFmpegMediaPlayer(
      @NotNull final FrameCallback callback,
      @NotNull final Dimension pixelDimension,
      @NotNull final String url,
      @Nullable final String key,
      final int fps) {
    super(callback, pixelDimension, url, key, fps);
    this.initializePlayer(0L);
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls) {
    super.setPlayerState(controls);
    switch (controls) {
      case START -> {
        if (this.ffmpeg == null) {
          this.initializePlayer(0L);
        }
        this.future = this.ffmpeg.executeAsync(ExecutorProvider.FFMPEG_VIDEO_PLAYER);
        this.start = System.currentTimeMillis();
        this.audio = true;
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
        this.future = this.ffmpeg.executeAsync(ExecutorProvider.FFMPEG_VIDEO_PLAYER);
        this.audio = true;
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

    final Dimension dimension = getDimensions();
    final String url = this.getUrl();
    final Path path = Path.of(url);
    final long ms = seconds * 1000;
    int delay;

    try {
      delay = 1000 / VideoFrameUtils.getFrameRate(this.getCallback().getCore(), path).orElse(30);
    } catch (final IOException e) {
      Logger.error(
          "A severe error occurred with extracting the fps of a video! Resorting to 30 fps!");
      delay = 30;
      e.printStackTrace();
    }

    this.ffmpeg =
        new FFmpeg(this.getCore().getFFmpegPath())
            .addInput(
                Files.exists(path)
                    ? UrlInput.fromPath(path).setPosition(ms)
                    : UrlInput.fromUrl(url).setPosition(ms))
            .addOutput(
                FrameOutput.withConsumer(
                        this.getFrameConsumer(this.getCallback(), this.getDimensions(), delay))
                    .setFrameRate(this.getFrameRate())
                    .disableStream(StreamType.AUDIO)
                    .disableStream(StreamType.SUBTITLE)
                    .disableStream(StreamType.DATA)).addArguments("-vf", "scale=%s:%s".formatted(dimension.getWidth(), dimension.getHeight()));
  }

  @Override
  public long getElapsedMilliseconds() {
    return System.currentTimeMillis() - this.start;
  }

  private FrameConsumer getFrameConsumer(
      final Callback callback, final Dimension dimension, final int delay) {
    final int width = dimension.getWidth();
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

        long before = System.currentTimeMillis();

        callback.process(image.getRGB(0, 0, width, dimension.getHeight(), null, 0, width));

        try {
          long wait = delay - (System.currentTimeMillis() - before) - 1;
          if (wait <= 0) {
            return; // go to next frame because too delayed
          }
          Thread.sleep(wait);
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }

        if (FFmpegMediaPlayer.this.audio) {
          FFmpegMediaPlayer.this.playAudio();
          FFmpegMediaPlayer.this.audio = false;
        }
      }
    };
  }

  // working on method
  public int[] getRGBFast(@NotNull final BufferedImage image) {

    final int width = image.getWidth();
    final int height = image.getHeight();
    final int[] array = new int[width * height];
    final Raster raster = image.getRaster();
    final ColorModel model = image.getColorModel();
    final int nbands = raster.getNumBands();

    final Object data = switch (raster.getDataBuffer().getDataType()) {
      case DataBuffer.TYPE_BYTE -> new byte[nbands];
      case DataBuffer.TYPE_USHORT -> new short[nbands];
      case DataBuffer.TYPE_INT -> new int[nbands];
      case DataBuffer.TYPE_FLOAT -> new float[nbands];
      case DataBuffer.TYPE_DOUBLE -> new double[nbands];
      default -> throw new IllegalArgumentException("Unknown data buffer type!");
    };

    final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    final int tasks = (height / 64) + 1;
    for (int task = 0; task < tasks; task++) {
      final int startingY = task * 64;
      executor.submit(() -> {
        int offset = width * startingY;
        int index;
        for (int y = startingY; y < height; y++, offset += width) {
          index = offset;
          for (int x = 0; x < width; x++) {
            array[index++] = model.getRGB(raster.getDataElements(x, y, data));
          }
        }
      });
    }


//    Old Loop (Synchronous)
//    int offset = 0;
//    int index;
//    for (int y = 0; y < height; y++, offset += width) {
//      index = offset;
//      for (int x = 0; x < width; x++) {
//        array[index++] = model.getRGB(raster.getDataElements(x, y, data));
//      }
//    }

    return array;
  }
}
