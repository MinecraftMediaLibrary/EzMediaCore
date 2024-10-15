package io.github.pulsebeat02.ezmediacore.transcode;

import org.bytedeco.javacv.*;
import io.github.pulsebeat02.ezmediacore.pipeline.input.Input;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FFmpegTrimmer {

  private final Input input;
  private final ExecutorService service;

  public FFmpegTrimmer(final Input input, final ExecutorService service) {
    this.input = input;
    this.service = service;
  }

  public FFmpegTrimmer(final Input input) {
    this(input, Executors.newSingleThreadExecutor());
  }

  public CompletableFuture<Path> trim(final Path target, final long start, final long end) {
    try (this.service) {
      return CompletableFuture.supplyAsync(() -> this.transcode(target, start, end), this.service);
    }
  }

  private Path transcode(final Path target, final long start, final long end) {
    final CompletableFuture<String> cf = this.input.getMediaRepresentation();
    final String input = cf.join();
    final String output = target.toString();
    try (final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input)) {
      grabber.start();
      grabber.setTimestamp(start);
      final int width = grabber.getImageWidth();
      final int height = grabber.getImageHeight();
      try (final FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output, width, height)) {
        recorder.start();
        Frame frame;
        while ((frame = grabber.grab()) != null) {
          final long current = grabber.getTimestamp();
          if (current > end) {
            break;
          }
          recorder.record(frame);
        }
      }
    } catch (final FrameGrabber.Exception | FrameRecorder.Exception e) {
      throw new AssertionError(e);
    }
    return target;
  }
}
