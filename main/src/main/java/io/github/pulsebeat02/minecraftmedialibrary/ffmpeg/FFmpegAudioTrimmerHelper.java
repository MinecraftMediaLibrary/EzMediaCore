/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.ffmpeg;

import com.google.common.collect.ImmutableList;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FFmpegAudioTrimmerHelper implements AudioTrimmerContext {

  private final List<String> arguments;
  private final Path input;
  private final Path output;
  private final long start;

  /**
   * Instantiates a new FFmpegAudioTrimmerHelper.
   *
   * @param input the input
   * @param output the output
   */
  public FFmpegAudioTrimmerHelper(
      @NotNull final Path input, @NotNull final Path output, final long start) {
    this.input = input.toAbsolutePath();
    this.output = output.toAbsolutePath();
    this.start = start;
    arguments = generateArguments();
    if (Files.exists(output)) {
      try {
        Files.delete(output);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Generates arguments from the start time.
   *
   * @return the UrlOutput
   */
  private List<String> generateArguments() {
    return new ArrayList<>(
        ImmutableList.<String>builder()
            .add(FFmpegDependencyInstallation.getFFmpegPath().toString())
            .add("-ss", String.valueOf(start))
            .add("-i", input.toString(), output.toString())
            .build());
  }

  /**
   * Adds arguments to the command.
   *
   * @param args the arguments
   */
  public void addArguments(@NotNull final String... args) {
    arguments.addAll(Arrays.stream(args).collect(Collectors.toList()));
  }

  @Override
  public void trim() {
    final ProcessBuilder builder = new ProcessBuilder(arguments);
    builder.redirectErrorStream(true);
    try {
      final Process p = builder.start();
      Logger.info(String.format("Starting Trimming Process with Arguments: %s", arguments));
      try (final BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line;
        while (true) {
          line = r.readLine();
          if (line == null) {
            break;
          }
          Logger.info(line);
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    Logger.info("Finished Trimming Process..!");
  }

  @Override
  public Path getInput() {
    return input;
  }

  @Override
  public Path getOutput() {
    return output;
  }

  @Override
  public List<String> getArguments() {
    return arguments;
  }

  /**
   * Checks if the current instance and object are equal.
   *
   * @param obj the other object
   * @return whether the current instance and other object are equal
   */
  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof FFmpegAudioExtractionHelper)) {
      return false;
    }
    final FFmpegAudioExtractionHelper extraction = (FFmpegAudioExtractionHelper) obj;
    return input.equals(extraction.getInput())
        && output.equals(extraction.getOutput())
        && arguments.equals(extraction.getArguments());
  }
}
