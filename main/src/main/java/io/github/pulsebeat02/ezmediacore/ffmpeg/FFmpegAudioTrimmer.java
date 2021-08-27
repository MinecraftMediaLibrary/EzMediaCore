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
import io.github.pulsebeat02.ezmediacore.format.FormatterProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
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
    this.input = input.toAbsolutePath();
    this.output = output.toAbsolutePath();
    this.clearArguments();
    this.addMultipleArguments(this.generateArguments());
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
    return new ArrayList<>(List.of(
        this.getCore().getFFmpegPath().toString(),
        "-ss", FormatterProvider.FFMPEG_TIME_FORMATTER.format(new Date(this.ms)),
        "-to", "99:99:99.999",
        "-i", this.input.toString(),
        this.output.toString()
    ));
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
}
