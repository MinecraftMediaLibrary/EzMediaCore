package rewrite.transcode;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class FFmpegTranscoder {

  private final Path input;

  public FFmpegTranscoder(final Path input) {
    this.input = input;
  }

  public CompletableFuture<Path> transcode(final Path output, final String format) throws IOException {




    final String input = this.input.toString();
    final String output = output.toString();
    final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
    final FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(this.outputPath.toString(), grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels()))


      grabber.start();
      recorder.setFormat("ogg");
      recorder.setSampleRate(grabber.getSampleRate());
      recorder.setFrameRate(grabber.getFrameRate());
      recorder.start();

      Frame frame;
      while ((frame = grabber.grab()) != null) {
        recorder.record(frame);
      }

      recorder.stop();
      grabber.stop();
    } catch (final Exception e) {
      throw new IOException("Failed to transcode file", e);
    }
  }
}
