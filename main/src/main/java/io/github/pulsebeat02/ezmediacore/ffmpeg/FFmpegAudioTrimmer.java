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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class FFmpegAudioTrimmer extends FFmpegCommandExecutor implements AudioTrimmer {

  private final String input;
  private final String output;
  private final long ms;

  FFmpegAudioTrimmer(
      @NotNull final MediaLibraryCore core,
      @NotNull final String input,
      @NotNull final String output,
      final long ms) {
    super(core);
    this.input = input;
    this.output = output;
    this.clearArguments();
    this.addMultipleArguments(this.generateArguments());
    this.ms = ms;
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull FFmpegAudioTrimmer ofFFmpegAudioTrimmer(
      @NotNull final MediaLibraryCore core,
      @NotNull final String input,
      @NotNull final String output,
      final long ms) {
    return new FFmpegAudioTrimmer(core, input, output, ms);
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull FFmpegAudioTrimmer ofFFmpegAudioTrimmer(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path input,
      @NotNull final Path output,
      final long ms) {
    return ofFFmpegAudioTrimmer(core, input.toString(), output.toString(), ms);
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull FFmpegAudioTrimmer ofFFmpegAudioTrimmer(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path input,
      @NotNull final String fileName,
      final long ms) {
    return ofFFmpegAudioTrimmer(core, input, core.getAudioPath().resolve(fileName), ms);
  }

  @Contract(" -> new")
  private @NotNull List<String> generateArguments() {
    final String path = this.getCore().getFFmpegPath().toString();
    final String time = FormatterProvider.FFMPEG_TIME_FORMATTER.format(new Date(this.ms));
    return new ArrayList<>(
        List.of(path, "-ss", time, "-to", "99:99:99.999", "-i", this.input, this.output));
  }

  @Override
  public @NotNull String getInput() {
    return this.input;
  }

  @Override
  public @NotNull String getOutput() {
    return this.output;
  }

  @Override
  public long getStartTime() {
    return this.ms;
  }
}
