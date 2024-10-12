package rewrite.pipeline.grabbers;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import rewrite.pipeline.FramePipelineResult;
import rewrite.pipeline.frame.BasicPacket;
import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.input.Input;
import rewrite.util.ExecutorUtils;

import java.awt.image.BufferedImage;
import java.nio.ShortBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FFmpegGrabberPlayer implements GrabberPlayer<FramePacket> {

  private final ExecutorService executor;
  private final FramePipelineResult result;
  private FFmpegFrameGrabber grabber;

  private Input source;
  private int width;
  private int height;

  private volatile boolean paused;
  private volatile Frame captured;

  public FFmpegGrabberPlayer(final FramePipelineResult result) {
    this(result, Executors.newSingleThreadExecutor());
  }

  public FFmpegGrabberPlayer(final FramePipelineResult result, final ExecutorService executor) {
    this.result = result;
    this.executor = executor;
  }

  @Override
  public void play(final Input source, final String[] arguments) {
    this.recreateGrabber(source);
    this.consumeFrames();
  }

  private void recreateGrabber(final Input source) {
    if (this.grabber != null) {
      this.release();
    }
    final String raw = source.getMediaRepresentation();
    this.source = source;
    this.grabber = new FFmpegFrameGrabber(raw);
  }

  private void consumeFrames() {
    try {
      this.grabber.start();
      this.width = this.grabber.getImageWidth();
      this.height = this.grabber.getImageHeight();
      CompletableFuture.runAsync(this::grabCurrentFrame, this.executor);
    } catch (final FFmpegFrameGrabber.Exception e) {
      throw new AssertionError(e);
    }
  }

  private void grabCurrentFrame() {
    while (true) {
      try {
        if (this.paused || (this.captured = this.grabber.grab()) == null) {
          break;
        }
      } catch (final FFmpegFrameGrabber.Exception e) {
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
      this.grabber.stop();
    } catch (final FFmpegFrameGrabber.Exception e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void seek(final long position) {
    try {
      this.grabber.setTimestamp(position);
    } catch (final FFmpegFrameGrabber.Exception e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void release() {
    try {
      this.paused = true;
      this.grabber.stop();
      this.grabber.release();
      ExecutorUtils.shutdownExecutorGracefully(this.executor);
    } catch (final FFmpegFrameGrabber.Exception e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public Input getSource() {
    return this.source;
  }

  @Override
  public FramePacket grabOutputFrame() {
    final FramePacket packet = new BasicPacket(this.getRGBSamples(), this.getAudioSamples(), this.width, this.height);
    this.result.executePipeline(packet);
    return packet;
  }

  @Override
  public FramePipelineResult getPipeline() {
    return this.result;
  }

  private byte[] getAudioSamples() {
    final ShortBuffer channelSamplesShortBuffer = (ShortBuffer) this.captured.samples[0];
    channelSamplesShortBuffer.rewind();
    final byte[] samples = new byte[channelSamplesShortBuffer.capacity() * 2];
    for (int i = 0; i < channelSamplesShortBuffer.capacity(); i++) {
      final short val = channelSamplesShortBuffer.get(i);
      samples[i * 2] = (byte) (val & 0xff);
      samples[i * 2 + 1] = (byte) ((val >> 8) & 0xff);
    }
    return samples;
  }

  private int[] getRGBSamples() {
    final BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
    Java2DFrameConverter.copy(image, this.captured);
    final int[] rgbSamples = new int[this.width * this.height];
    image.getRGB(0, 0, this.width, this.height, rgbSamples, 0, this.width);
    return rgbSamples;
  }
}
