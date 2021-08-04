package io.github.pulsebeat02.ezmediacore.player;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResultFuture;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.FrameCallback;
import io.github.pulsebeat02.ezmediacore.utility.ImmutableDimension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class FFmpegMediaPlayer extends MediaPlayer {

  private FFmpeg ffmpeg;
  private FFmpegResultFuture future;
  private long start;

  protected FFmpegMediaPlayer(
      @NotNull final MediaLibraryCore core,
      @NotNull final FrameCallback callback,
      @NotNull final ImmutableDimension dimensions,
      @NotNull final String url,
      final int frameRate) {
    super(core, callback, dimensions, url, frameRate);
    this.initializePlayer(0L);
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls) {
    super.setPlayerState(controls);
    switch (controls) {
      case START:
        if (this.ffmpeg == null) {
          this.initializePlayer(0L);
        }
        this.future = this.ffmpeg.executeAsync();
        this.playAudio();
        this.start = System.currentTimeMillis();
        break;
      case PAUSE:
        if (this.ffmpeg != null) {
          this.future.graceStop();
        }
        this.stopAudio();
        this.start = System.currentTimeMillis();
        break;
      case RESUME:
        this.initializePlayer(System.currentTimeMillis() - this.start);
        this.future = this.ffmpeg.executeAsync();
        this.playAudio();
        break;
      case RELEASE:
        if (this.ffmpeg != null) {
          this.future.graceStop();
          this.future = null;
        }
        break;
    }
  }

  @Override
  public void initializePlayer(final long seconds) {
    final String url = this.getUrl();
    final Path path = Paths.get(url);
    final long ms = seconds * 1000;
    this.ffmpeg =
        new FFmpeg(this.getCore().getFFmpegPath())
            .addInput(
                Files.exists(path)
                    ? UrlInput.fromPath(path).setPosition(ms)
                    : UrlInput.fromUrl(url).setPosition(ms))
            .addOutput(
                FrameOutput.withConsumer(
                        this.getFrameConsumer(this.getCallback(), this.getDimensions()))
                    .setFrameRate(this.getFrameRate())
                    .disableStream(StreamType.AUDIO)
                    .disableStream(StreamType.SUBTITLE)
                    .disableStream(StreamType.DATA));
  }

  @Override
  public long getElapsedMilliseconds() {
    return System.currentTimeMillis() - this.start;
  }

  private FrameConsumer getFrameConsumer(
      final Callback callback, final ImmutableDimension dimension) {
    final int width = dimension.getWidth();
    return new FrameConsumer() {
      @Override
      public void consumeStreams(final List<Stream> streams) {}

      @Override
      public void consume(final Frame frame) {
        if (frame == null) {
          return;
        }
        callback.process(
            frame.getImage().getRGB(0, 0, width, dimension.getHeight(), null, 0, width));
      }
    };
  }
}
