package rewrite.transcode;

import org.bytedeco.javacv.*;
import rewrite.pipeline.input.Input;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FFmpegAudioExtractor {

  private final Input input;
  private final ExecutorService service;

  public FFmpegAudioExtractor(final Input input, final ExecutorService service) {
    this.input = input;
    this.service = service;
  }

  public FFmpegAudioExtractor(final Input input) {
    this(input, Executors.newSingleThreadExecutor());
  }

  public CompletableFuture<Path> extractAudio(final Path target, final String codec, final String format) {
    try (this.service) {
      return CompletableFuture.supplyAsync(() -> this.transcode(target, codec, format), this.service);
    }
  }

  private Path transcode(final Path target, final String codec, final String format) {
    final CompletableFuture<String> cf = this.input.getMediaRepresentation();
    final String input = cf.join();
    final String output = target.toString();
    try (final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input)) {
      grabber.start();
      final int width = grabber.getImageWidth();
      final int height = grabber.getImageHeight();
      final int channels = grabber.getAudioChannels();
      final int sampleRate = grabber.getSampleRate();
      final double frameRate = grabber.getFrameRate();
      try (final FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output, width, height, channels)) {
        recorder.setFormat(format);
        recorder.setAudioCodecName(codec);
        recorder.setSampleRate(sampleRate);
        recorder.setFrameRate(frameRate);
        recorder.start();
        Frame frame;
        while ((frame = grabber.grab()) != null) {
          recorder.record(frame);
        }
      }
    } catch (final FrameGrabber.Exception | FrameRecorder.Exception e) {
      throw new AssertionError(e);
    }
    return target;
  }
}
