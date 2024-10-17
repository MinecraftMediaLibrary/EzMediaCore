package io.github.pulsebeat02.ezmediacore.pipeline.grabbers.ffmpeg;

import io.github.pulsebeat02.ezmediacore.dimension.Resolution;
import io.github.pulsebeat02.ezmediacore.pipeline.FramePipelineResult;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.FramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.grabbers.GrabberPlayer;
import io.github.pulsebeat02.ezmediacore.pipeline.input.Input;
import io.github.pulsebeat02.ezmediacore.util.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FFmpegGrabberPlayer implements GrabberPlayer<FramePacket> {

  private final FramePipelineResult result;
  private final ExecutorService executor;
  private Map<String, String> resolutionArguments;

  private volatile GrabberPlayer<FramePacket> player;

  public FFmpegGrabberPlayer(final FramePipelineResult result) {
    this(result, Executors.newSingleThreadExecutor());
  }

  public FFmpegGrabberPlayer(final FramePipelineResult result, final ExecutorService executor) {
    this.result = result;
    this.executor = executor;
  }

  public void setResolution(final Resolution resolution) {
    final int width = resolution.getWidth();
    final int height = resolution.getHeight();
    this.resolutionArguments = Map.of("-vf", "scale=" + width + ":" + height);
  }

  @Override
  public void play(final Input video, final Input audio, final Map<String, String> arguments) {
    final Map<String, String> args = this.constructFinalArguments(arguments);
    this.player = new FFmpegDualFrameGrabber(this.result, this.executor);
    this.player.play(video, audio, args);
  }

  @Override
  public void play(final Input source, final Map<String, String> arguments) {
    final Map<String, String> args = this.constructFinalArguments(arguments);
    this.player = new FFmpegSingleFrameGrabber(this.result, this.executor);
    this.player.play(source, args);
  }

  private Map<String, String> constructFinalArguments(final Map<String, String> arguments) {
    final Map<String, String> pre = this.resolutionArguments == null ? Map.of() : this.resolutionArguments;
    return CollectionUtils.merge(pre, arguments);
  }

  @Override
  public void resume() {
    if (this.player != null) {
      this.player.resume();
    }
  }

  @Override
  public void pause() {
    if (this.player != null) {
      this.player.pause();
    }
  }

  @Override
  public void seek(final long position) {
    if (this.player != null) {
      this.player.seek(position);
    }
  }

  @Override
  public void release() {
    if (this.player != null) {
      this.player.release();
      this.player = null;
    }
  }

  @Override
  public Collection<Input> getSources() {
    if (this.player != null) {
      return this.player.getSources();
    }
    return new ArrayList<>();
  }

  @Override
  public FramePacket grabOutputFrame() {
    if (this.player != null) {
      return this.player.grabOutputFrame();
    }
    return null;
  }

  @Override
  public FramePipelineResult getPipeline() {
    return this.result;
  }
}
