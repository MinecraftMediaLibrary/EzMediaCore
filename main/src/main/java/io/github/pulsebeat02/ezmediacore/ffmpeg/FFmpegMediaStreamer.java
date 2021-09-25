package io.github.pulsebeat02.ezmediacore.ffmpeg;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FFmpegMediaStreamer extends FFmpegCommandExecutor implements IOProvider {

  private final String input;
  private final String output;

  public FFmpegMediaStreamer(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String input,
      @NotNull final String output) {
    super(core);
    this.input = input;
    this.output = output;
    this.clearArguments();
    this.addMultipleArguments(this.generateArguments(configuration));
  }

  @Contract("_ -> new")
  private @NotNull List<String> generateArguments(@NotNull final AudioConfiguration configuration) {
    return new ArrayList<>(
        List.of(
            this.getCore().getFFmpegPath().toString(),
            "-i",
            this.input,
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
            String.valueOf(configuration.getStartTime())));
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) {
    this.addArgument(this.output);
    super.executeWithLogging(logger);
  }

  @Override
  public @NotNull String getInput() {
    return this.input;
  }

  @Override
  public @NotNull String getOutput() {
    return this.output;
  }
}
