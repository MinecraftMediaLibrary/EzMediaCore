package io.github.pulsebeat02.ezmediacore.ffmpeg;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FFmpegAudioExtractor extends FFmpegCommandExecutor implements AudioExtractor {

  private final Path input;
  private final Path output;

  public FFmpegAudioExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final Path input,
      @NotNull final Path output) throws IOException {
    super(core);
    this.input = input.toAbsolutePath();
    this.output = output.toAbsolutePath();
    this.clearArguments();
    this.addMultipleArguments(this.generateArguments(configuration));
  }

  private List<String> generateArguments(@NotNull final AudioConfiguration configuration) {
    final String in = this.input.toString();
    return new ArrayList<>(
        List.of(
            this.getCore().getFFmpegPath().toString(),
            "-f",
            FilenameUtils.getExtension(in),
            "-i",
            in,
            "-vn",
            "-acodec",
            "libvorbis",
            "-ab",
            String.valueOf(configuration.getBitrate()),
            "-ac",
            String.valueOf(configuration.getChannels()),
            "-ar",
            String.valueOf(configuration.getSamplingRate()),
            "-vol",
            String.valueOf(configuration.getVolume()),
            "-ss",
            String.valueOf(configuration.getStartTime()),
            "-f",
            FilenameUtils.getExtension(this.output.toString())));
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) {
    this.addArguments("-y", this.output.toString());
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
  public boolean equals(final Object obj) {
    if (!(obj instanceof final FFmpegAudioExtractor extraction)) {
      return false;
    }
    return this.input.equals(extraction.getInput())
        && this.output.equals(extraction.getOutput())
        && super.equals(obj);
  }
}
