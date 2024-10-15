package rewrite.pipeline.grabbers;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import rewrite.pipeline.FramePipelineResult;
import rewrite.pipeline.frame.BasicFramePacket;
import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.input.Input;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FFmpegGifGrabberPlayer implements GrabberPlayer<FramePacket> {

  private final FramePipelineResult result;

  private final ExecutorService executor;

  private FFmpegFrameGrabber videoGrabber;

  private final Collection<Input> sources;
  private int width;
  private int height;

  private volatile boolean paused;
  private volatile Frame captured;

  public FFmpegGifGrabberPlayer(final FramePipelineResult result) {
    this(result, Executors.newSingleThreadExecutor());
  }

  public FFmpegGifGrabberPlayer(final FramePipelineResult result, final ExecutorService executor) {
    this.sources = new ArrayList<>();
    this.result = result;
    this.executor = executor;
  }

  @Override
  public void play(final Input video, final Input audio, final Map<String, String> arguments) {
    CompletableFuture.runAsync(() -> this.recreateGrabber(video, arguments), this.executor)
            .thenRun(this::consumeFrames);
  }

  @Override
  public void play(final Input source, final Map<String, String> arguments) {
    this.play(source, null, arguments);
  }

  private void recreateGrabber(final Input video, final Map<String, String> arguments) {
    this.release();
    final CompletableFuture<String> rawVideo = video.getMediaRepresentation();
    final String retrievedVideo = rawVideo.join();
    this.videoGrabber = new FFmpegFrameGrabber(retrievedVideo);
    this.videoGrabber.setOptions(arguments);
    this.sources.add(video);
  }

  private void consumeFrames() {
    try {
      this.videoGrabber.setOption("loop", "0");
      this.videoGrabber.start();
      this.width = this.videoGrabber.getImageWidth();
      this.height = this.videoGrabber.getImageHeight();
      CompletableFuture.runAsync(this::grabCurrentFrame, this.executor);
    } catch (final FFmpegFrameGrabber.Exception e) {
      throw new AssertionError(e);
    }
  }

  private void grabCurrentFrame() {
    while (true) {
      try {
        if (this.paused) {
          break;
        }
        final Frame frame = this.videoGrabber.grabAtFrameRate();
        if (frame == null) {
          break;
        }
      } catch (final InterruptedException | FrameGrabber.Exception e) {
        throw new AssertionError(e);
      }
    }
  }

  @Override
  public void resume() {
    this.consumeFrames();
  }

  @Override
  public void pause() {
    try {
      this.paused = true;
      this.videoGrabber.stop();
    } catch (final FFmpegFrameGrabber.Exception e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void seek(final long position) {
    try {
      this.videoGrabber.setTimestamp(position);
    } catch (final FFmpegFrameGrabber.Exception e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void release() {
    try {
      this.paused = true;
      this.sources.clear();
      if (this.videoGrabber != null) {
        this.videoGrabber.stop();
        this.videoGrabber.release();
        this.videoGrabber = null;
      }
      this.result.releasePipelines();
    } catch (final FFmpegFrameGrabber.Exception e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public Collection<Input> getSources() {
    return this.sources;
  }

  @Override
  public FramePacket grabOutputFrame() {
    final FramePacket packet = new BasicFramePacket(this.getRGBSamples(), null, this.width, this.height, null);
    this.result.executePipeline(packet);
    return packet;
  }

  @Override
  public FramePipelineResult getPipeline() {
    return this.result;
  }

  private int[] getRGBSamples() {
    final BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
    Java2DFrameConverter.copy(image, this.captured);
    final int[] rgbSamples = new int[this.width * this.height];
    image.getRGB(0, 0, this.width, this.height, rgbSamples, 0, this.width);
    return rgbSamples;
  }
}
