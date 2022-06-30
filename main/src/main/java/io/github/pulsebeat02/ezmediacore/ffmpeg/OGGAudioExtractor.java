/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.ffmpeg;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OGGAudioExtractor extends FFmpegCommandExecutor implements AudioExtractor {

  private final String input;
  private final String output;

  OGGAudioExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String input,
      @NotNull final String output) {
    super(core);
    this.input = input;
    this.output = output;
    this.clearArguments();
    this.generateArguments(configuration);
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull OGGAudioExtractor ofFFmpegAudioExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String input,
      @NotNull final String output) {
    return new OGGAudioExtractor(core, configuration, input, output);
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull OGGAudioExtractor ofFFmpegAudioExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final Path input,
      @NotNull final Path output) {
    return ofFFmpegAudioExtractor(core, configuration, input.toString(), output.toString());
  }

  private void generateArguments(@NotNull final AudioConfiguration configuration) {

    final String path = this.getCore().getFFmpegPath().toString();
    this.addArgument(path);

    this.addArgument(FFmpegArguments.HIDE_BANNER);
    this.addArgument(FFmpegArguments.NO_STATS);
    this.addArguments(FFmpegArguments.LOG_LEVEL, "error");
    this.addArguments(FFmpegArguments.INPUT, this.input);
    this.addArgument(FFmpegArguments.EXCLUDE_VIDEO_STREAMS);
    this.addArguments(FFmpegArguments.AUDIO_CODEC, "libvorbis");

    final String bitrate = String.valueOf(configuration.getBitrate());
    this.addArguments(FFmpegArguments.AUDIO_BITRATE, bitrate);

    final String channels = String.valueOf(configuration.getChannels());
    this.addArguments(FFmpegArguments.AUDIO_CHANNELS, channels);

    final String sampling = String.valueOf(configuration.getSamplingRate());
    this.addArguments(FFmpegArguments.AUDIO_SAMPLING, sampling);

    final String volume = String.valueOf(configuration.getVolume());
    this.addArguments(FFmpegArguments.AUDIO_VOLUME, volume);

    final String start = String.valueOf(configuration.getStartTime());
    this.addArguments(FFmpegArguments.DURATION_START, start);

    this.addArgument(FFmpegArguments.OVERWRITE_FILE);
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) throws IOException {
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
