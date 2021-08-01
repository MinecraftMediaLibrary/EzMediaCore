package io.github.pulsebeat02.epicmedialib.ffmpeg;

import com.google.common.collect.ImmutableList;
import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import io.github.pulsebeat02.epicmedialib.extraction.AudioConfiguration;
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
      @NotNull final Path output) {
    super(core);
    clearArguments();
    addMultipleArguments(generateArguments(configuration));
    this.input = input.toAbsolutePath();
    this.output = output.toAbsolutePath();
  }

  private List<String> generateArguments(@NotNull final AudioConfiguration configuration) {
    final String in = this.input.toString();
    return new ArrayList<>(
        ImmutableList.<String>builder()
            .add(getCore().getFFmpegPath().toString())
            .add("-f", FilenameUtils.getExtension(in))
            .add("-i", in)
            .add("-vn")
            .add("-acodec", "libvorbis")
            .add("-ab", String.valueOf(configuration.getBitrate()))
            .add("-ac", String.valueOf(configuration.getChannels()))
            .add("-ar", String.valueOf(configuration.getSamplingRate()))
            .add("-vol", String.valueOf(configuration.getVolume()))
            .add("-ss", String.valueOf(configuration.getStartTime()))
            .add("-f", FilenameUtils.getExtension(this.output.toString()))
            .build());
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) {
    addArguments("-y", this.output.toString());
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
    if (!(obj instanceof FFmpegAudioExtractor)) {
      return false;
    }
    final FFmpegAudioExtractor extraction = (FFmpegAudioExtractor) obj;
    return this.input.equals(extraction.getInput())
        && this.output.equals(extraction.getOutput())
        && super.equals(obj);
  }
}
