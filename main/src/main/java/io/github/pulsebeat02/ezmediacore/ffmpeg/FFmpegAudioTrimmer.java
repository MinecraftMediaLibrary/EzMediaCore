package io.github.pulsebeat02.ezmediacore.ffmpeg;

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
  private final long ms;

  public FFmpegAudioTrimmer(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path input,
      @NotNull final Path output,
      final long ms) {
    super(core);
    this.clearArguments();
    this.addMultipleArguments(this.generateArguments());
    this.input = input.toAbsolutePath();
    this.output = output.toAbsolutePath();
    this.ms = ms;
  }

  public FFmpegAudioTrimmer(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path input,
      @NotNull final String fileName,
      final long ms) {
    this(core, input, core.getAudioPath().resolve(fileName), ms);
  }

  private List<String> generateArguments() {
    return new ArrayList<>(
        List.of(
            this.getCore().getFFmpegPath().toString(),
            "-ss",
            "%d.ms".formatted(this.ms),
            "-i",
            this.input.toString(),
            this.output.toString()));
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
    return this.ms;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof final FFmpegAudioTrimmer extraction)) {
      return false;
    }
    return this.input.equals(extraction.getInput())
        && this.output.equals(extraction.getOutput())
        && this.ms == extraction.getStartTime()
        && super.equals(obj);
  }
}
