package rewrite.pipeline.grabbers;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import rewrite.pipeline.FramePipelineResult;
import rewrite.pipeline.frame.BasicPacket;
import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.input.Input;

import javax.sound.sampled.AudioFormat;
import java.awt.image.BufferedImage;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FFmpegGrabberPlayer implements GrabberPlayer<FramePacket> {

  private final FramePipelineResult result;

  private final ExecutorService executor;

  private FFmpegFrameGrabber videoGrabber;
  private FFmpegFrameGrabber audioGrabber;
  private AudioFormat format;

  private final Collection<Input> sources;
  private int width;
  private int height;

  private volatile boolean paused;
  private volatile Frame capturedVideo;
  private volatile Frame capturedAudio;

  public FFmpegGrabberPlayer(final FramePipelineResult result) {
    this(result, Executors.newSingleThreadExecutor());
  }

  public FFmpegGrabberPlayer(final FramePipelineResult result, final ExecutorService executor) {
    this.sources = new ArrayList<>();
    this.result = result;
    this.executor = executor;
  }

  @Override
  public void play(final Input video, final Input audio, final Map<String, String> arguments) {
    CompletableFuture.runAsync(() -> this.recreateGrabber(video, audio, arguments), this.executor)
            .thenRun(this::consumeFrames);
  }

  @Override
  public void play(final Input source, final Map<String, String> arguments) {
    this.play(source, null, arguments);
  }

  private void recreateGrabber(final Input video, final Input audio, final Map<String, String> arguments) {
    this.release();
    final CompletableFuture<String> rawVideo = video.getMediaRepresentation();
    final String retrievedVideo = rawVideo.join();
    this.videoGrabber = new FFmpegFrameGrabber(retrievedVideo);
    this.videoGrabber.setOptions(arguments);
    this.sources.add(video);
    if (audio != null) {
      final CompletableFuture<String> rawAudio = audio.getMediaRepresentation();
      final String retrievedAudio = rawAudio.join();
      this.audioGrabber = new FFmpegFrameGrabber(retrievedAudio);
      this.audioGrabber.setOptions(arguments);
      this.sources.add(audio);
    }
  }

  private void consumeFrames() {
    try {
      this.videoGrabber.start();
      if (this.audioGrabber != null) {
        this.audioGrabber.start();
      }
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
        this.capturedVideo = frame;
        if (this.audioGrabber != null) {
          this.capturedAudio = this.audioGrabber.grabAtFrameRate();
          if (this.capturedAudio == null) {
            break;
          }
        } else {
          this.capturedAudio = frame;
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
      if (this.audioGrabber != null) {
        this.audioGrabber.stop();
      }
    } catch (final FFmpegFrameGrabber.Exception e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void seek(final long position) {
    try {
      this.videoGrabber.setTimestamp(position);
      if (this.audioGrabber != null) {
        this.audioGrabber.setTimestamp(position);
      }
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
      if (this.audioGrabber != null) {
        this.audioGrabber.stop();
        this.audioGrabber.release();
        this.audioGrabber = null;
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
    final FramePacket packet = new BasicPacket(this.getRGBSamples(), this.getAudioSamples(), this.width, this.height, this.capturedAudio);
    this.result.executePipeline(packet);
    return packet;
  }

  @Override
  public FramePipelineResult getPipeline() {
    return this.result;
  }

  private byte[] getAudioSamples() {
    final ShortBuffer channelSamplesShortBuffer = (ShortBuffer) this.capturedAudio.samples[0];
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
    Java2DFrameConverter.copy(image, this.capturedVideo);
    final int[] rgbSamples = new int[this.width * this.height];
    image.getRGB(0, 0, this.width, this.height, rgbSamples, 0, this.width);
    return rgbSamples;
  }
}
