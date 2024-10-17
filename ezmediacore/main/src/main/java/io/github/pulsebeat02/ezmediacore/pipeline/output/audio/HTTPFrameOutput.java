package io.github.pulsebeat02.ezmediacore.pipeline.output.audio;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.FramePacket;
import io.github.pulsebeat02.ezmediacore.pipeline.output.FrameOutputSource;

import java.util.concurrent.atomic.AtomicBoolean;

public final class HTTPFrameOutput implements FrameOutputSource<FramePacket>  {

  private final String outputUrl;
  private final AtomicBoolean started;

  private FFmpegFrameRecorder recorder;

  public HTTPFrameOutput(final String outputUrl) {
    this.started = new AtomicBoolean(false);
    this.outputUrl = outputUrl;
  }

  @Override
  public void output(final FramePacket input) {
    if (!this.started.get()) {
      this.createRecorder(input);
      this.started.set(true);
    }
    this.recordFrame(input);
  }

  private void recordFrame(final FramePacket input) {
    try {
      final Frame cursed = (Frame) input.getMetadata();
      this.recorder.record(cursed);
    } catch (final FFmpegFrameRecorder.Exception e) {
      throw new AssertionError(e);
    }
  }

  private void createRecorder(final FramePacket input) {
    try {
      final int width = input.getImageWidth();
      final int height = input.getImageHeight();
      this.recorder = new FFmpegFrameRecorder(this.outputUrl, width, height);
      this.recorder.setFormat("mp3");
      this.recorder.setOption("content_type", "audio/mpeg");
      this.recorder.setOption("listen", "1");
      this.recorder.start();
    } catch (final FFmpegFrameRecorder.Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void release() {
    try {
      this.recorder.stop();
      this.recorder.release();
    } catch (final FFmpegFrameRecorder.Exception e) {
      throw new AssertionError(e);
    }
  }
}
