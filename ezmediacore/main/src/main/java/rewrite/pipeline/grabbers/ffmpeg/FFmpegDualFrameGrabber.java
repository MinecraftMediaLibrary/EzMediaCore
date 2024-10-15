package rewrite.pipeline.grabbers.ffmpeg;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
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

public final class FFmpegDualFrameGrabber implements GrabberPlayer<FramePacket> {

  private static final String FFMPEG_FILTER_FORMAT = "[0:a][1:a]amerge[a]";

  private final FramePipelineResult result;
  private final ExecutorService executor;
  private final Collection<Input> sources;

  private volatile FFmpegFrameGrabber videoGrabber;
  private volatile FFmpegFrameGrabber audioGrabber;
  private volatile FFmpegFrameFilter filter;
  private volatile int width;
  private volatile int height;
  private volatile int channels;
  private volatile boolean paused;
  private volatile Frame captured;

  public FFmpegDualFrameGrabber(final FramePipelineResult result) {
    this(result, Executors.newSingleThreadExecutor());
  }

  public FFmpegDualFrameGrabber(final FramePipelineResult result, final ExecutorService executor) {
    this.sources = new ArrayList<>();
    this.result = result;
    this.executor = executor;
  }

  @Override
  public void play(final Input video, final Input audio, final Map<String, String> arguments) {
    this.release();
    final CompletableFuture<String> rawVideo = video.getMediaRepresentation();
    final CompletableFuture<String> rawAudio = audio.getMediaRepresentation();
    final String retrievedVideo = rawVideo.join();
    final String retrievedAudio = rawAudio.join();
    this.sources.add(video);
    this.sources.add(audio);
    this.createGrabbers(arguments, retrievedVideo, retrievedAudio);
    this.consumeFrames();
  }

  @Override
  public void play(final Input source, final Map<String, String> arguments) {
    throw new UnsupportedOperationException("Dual frame grabber requires video and audio inputs");
  }

  private void createGrabbers(final Map<String, String> arguments, final String video, final String audio) {
    try {
      this.createVideoGrabber(arguments, video);
      this.createAudioGrabber(arguments, audio);
      this.createFilterGrabber();
    } catch (final FFmpegFrameFilter.Exception | FFmpegFrameGrabber.Exception e) {
      throw new AssertionError(e);
    }
  }

  private void createVideoGrabber(final Map<String, String> arguments, final String video) throws FFmpegFrameGrabber.Exception {
    this.videoGrabber = new FFmpegFrameGrabber(video);
    this.videoGrabber.setPixelFormat(avutil.AV_PIX_FMT_BGR32);
    this.audioGrabber.setOptions(arguments);
    this.videoGrabber.start();
    this.width = this.videoGrabber.getImageWidth();
    this.height = this.videoGrabber.getImageHeight();
  }

  private void createAudioGrabber(final Map<String, String> arguments, final String audio) throws FFmpegFrameGrabber.Exception {
    final GrabberAudioFormat standard = GrabberPlayer.AUDIO_FORMAT;
    this.audioGrabber = new FFmpegFrameGrabber(audio);
    this.audioGrabber.setAudioChannels(standard.getChannels());
    this.audioGrabber.setAudioCodec(avcodec.AV_CODEC_ID_OPUS); // opus format
    this.audioGrabber.setSampleRate(standard.getSampleRate());
    this.audioGrabber.setOptions(arguments);
    this.audioGrabber.setOption("f", standard.getSamplingFormat());
    this.audioGrabber.start();
    this.channels = this.audioGrabber.getAudioChannels();
  }

  private void createFilterGrabber() throws FFmpegFrameFilter.Exception {
    this.filter = new FFmpegFrameFilter(FFMPEG_FILTER_FORMAT, this.channels);
    this.filter.setAudioInputs(1);
    this.filter.setVideoInputs(1);
    this.filter.start();
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
        final Frame video = this.videoGrabber.grabAtFrameRate();
        final Frame audio = this.audioGrabber.grabAtFrameRate();
        this.filter.push(0, video);
        this.filter.push(1, audio);
        this.captured = this.filter.pull();
        this.grabOutputFrame();
      } catch (final InterruptedException | FrameGrabber.Exception | FFmpegFrameFilter.Exception e) {
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
      if (this.videoGrabber != null) {
        this.videoGrabber.stop();
      }
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
      if (this.filter != null) {
        this.filter.stop();
        this.filter.release();
        this.filter = null;
      }
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
    } catch (final FFmpegFrameGrabber.Exception | FFmpegFrameFilter.Exception e) {
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
