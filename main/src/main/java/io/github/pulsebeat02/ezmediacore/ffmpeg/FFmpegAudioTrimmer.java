package io.github.pulsebeat02.ezmediacore.ffmpeg;

import com.google.common.collect.ImmutableList;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FFmpegAudioTrimmer extends FFmpegCommandExecutor implements AudioTrimmer {

  private final Path input;
  private final Path output;
  private final long start;

  public FFmpegAudioTrimmer(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path input,
      @NotNull final Path output,
      final long start) {
    super(core);
    clearArguments();
    addMultipleArguments(generateArguments());
    this.input = input.toAbsolutePath();
    this.output = output.toAbsolutePath();
    this.start = start;
  }

  private List<String> generateArguments() {
    return new ArrayList<>(
        ImmutableList.<String>builder()
            .add(getCore().getFFmpegPath().toString())
            .add("-ss", String.valueOf(this.start))
            .add("-i", this.input.toString(), this.output.toString())
            .build());
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) {
    if (Files.exists(this.output)) {
      try {
        Files.delete(this.output);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
    super.executeWithLogging(logger);
  }

  @Override
  public @NotNull Path getInput() {
    return this.input;
  }

  @Override
  public @NotNull Path getOutput() {
    return this.output;
  }

  @Override
  public long getStartTime() {
    return this.start;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof FFmpegAudioTrimmer)) {
      return false;
    }
    final FFmpegAudioTrimmer extraction = (FFmpegAudioTrimmer) obj;
    return this.input.equals(extraction.getInput())
        && this.output.equals(extraction.getOutput())
        && this.start == extraction.getStartTime()
        && super.equals(obj);
  }
}
