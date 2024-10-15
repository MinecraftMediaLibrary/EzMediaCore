package rewrite.pipeline.grabbers.ffmpeg;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import rewrite.pipeline.FramePipelineResult;
import rewrite.pipeline.frame.BasicFramePacket;
import rewrite.pipeline.frame.FramePacket;
import rewrite.pipeline.grabbers.GrabberAudioFormat;
import rewrite.pipeline.grabbers.GrabberPlayer;
import rewrite.pipeline.input.Input;
import rewrite.util.graphics.FrameUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FFmpegSingleFrameGrabber implements GrabberPlayer<FramePacket> {

  private final FramePipelineResult result;
  private final ExecutorService executor;
  private final Collection<Input> sources;

  private volatile FFmpegFrameGrabber grabber;
  private volatile int width;
  private volatile int height;
  private volatile boolean paused;
  private volatile Frame captured;

  public FFmpegSingleFrameGrabber(final FramePipelineResult result) {
    this(result, Executors.newSingleThreadExecutor());
  }

  public FFmpegSingleFrameGrabber(final FramePipelineResult result, final ExecutorService executor) {
    this.sources = new ArrayList<>();
    this.result = result;
    this.executor = executor;
  }

  @Override
  public void play(final Input video, final Input audio, final Map<String, String> arguments) {
    throw new UnsupportedOperationException("Single frame grabber requires one input");
  }

  @Override
  public void play(final Input source, final Map<String, String> arguments) {
    this.release();
    final CompletableFuture<String> future = source.getMediaRepresentation();
    final String raw = future.join();
    this.sources.add(source);
    this.createGrabberExceptionally(arguments, raw);
    this.consumeFrames();
  }

  private void createGrabberExceptionally(final Map<String, String> arguments, final String input) {
    try {
      this.createGrabber(arguments, input);
    } catch (final FFmpegFrameGrabber.Exception e) {
      throw new AssertionError(e);
    }
  }

  private void createGrabber(final Map<String, String> arguments, final String input) throws FFmpegFrameGrabber.Exception {
    final GrabberAudioFormat standard = GrabberPlayer.AUDIO_FORMAT;
    this.grabber = new FFmpegFrameGrabber(input);
    this.grabber.setAudioChannels(standard.getChannels());
    this.grabber.setPixelFormat(avutil.AV_PIX_FMT_BGR32);
    this.grabber.setAudioCodec(avcodec.AV_CODEC_ID_OPUS); // opus format
    this.grabber.setSampleRate(standard.getSampleRate());
    this.grabber.setOptions(arguments);
    this.grabber.setOption("f", standard.getSamplingFormat());
    this.grabber.start();
    this.width = this.grabber.getImageWidth();
    this.height = this.grabber.getImageHeight();
  }

  private void consumeFrames() {
    CompletableFuture.runAsync(this::grabCurrentFrame, this.executor);
  }

  private void grabCurrentFrame() {
    while (true) {
      try {
        if (this.paused) {
          break;
        }
        this.captured = this.grabber.grabAtFrameRate();
        this.grabOutputFrame();
      } catch (final InterruptedException | FrameGrabber.Exception e) {
        throw new AssertionError(e);
      }
    }
  }

  @Override
  public void resume() {
    this.paused = false;
    this.consumeFrames();
  }

  @Override
  public void pause() {
    try {
      this.paused = true;
      if (this.grabber != null) {
        this.grabber.stop();
      }
    } catch (final FFmpegFrameGrabber.Exception e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void seek(final long position) {
    try {
      if (this.grabber != null) {
        this.grabber.setTimestamp(position);
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
      if (this.grabber != null) {
        this.grabber.stop();
        this.grabber.release();
        this.grabber = null;
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
    final int[] samples = FrameUtils.getRGBSamples(this.width, this.height, this.captured);
    final byte[] audioSamples = FrameUtils.getAudioSamples(this.captured);
    final FramePacket packet = new BasicFramePacket(samples, audioSamples, this.width, this.height, this.captured);
    this.result.executePipeline(packet);
    return packet;
  }

  @Override
  public FramePipelineResult getPipeline() {
    return this.result;
  }
}
