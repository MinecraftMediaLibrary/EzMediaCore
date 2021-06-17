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
import io.github.pulsebeat02.minecraftmedialibrary.extractor.ExtractionConfiguration;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.apache.commons.io.FilenameUtils;
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

/** An audio extraction helper that helps extract audio from video files. */
public class FFmpegAudioExtractionHelper implements AudioExtractionContext {

  private final List<String> arguments;
  private final Path input;
  private final Path output;

  /**
   * Instantiates a new AudioExtractionHelper.
   *
   * @param settings the settings
   * @param input the input
   * @param output the output
   */
  public FFmpegAudioExtractionHelper(
      @NotNull final ExtractionConfiguration settings,
      @NotNull final Path input,
      @NotNull final Path output) {
    this.input = input.toAbsolutePath();
    this.output = output.toAbsolutePath();
    arguments = generateArguments(settings);
    if (Files.exists(output)) {
      try {
        Files.delete(output);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Generates arguments from the ExtractionConfiguration. Example full command to extract ogg from
   * an mp4 file:
   *
   * <p>/Users/bli24/IdeaProjects/MinecraftMediaLibrary/ffmpeg-test/ffmpeg/ffmpeg-x86_64-osx -f mp4
   * -i /Users/bli24/IdeaProjects/MinecraftMediaLibrary/ffmpeg-test/video.mp4 -vn -acodec libvorbis
   * -ab 160000 -ac 2 -ar 44100 -vol 1 -f ogg -y
   * /Users/bli24/IdeaProjects/MinecraftMediaLibrary/ffmpeg-test/audio.ogg
   *
   * @param configuration the configuration
   * @return the UrlOutput
   */
  private List<String> generateArguments(@NotNull final ExtractionConfiguration configuration) {
    final String in = input.toString();
    return new ArrayList<>(
        ImmutableList.<String>builder()
            .add(FFmpegDependencyInstallation.getFFmpegPath().toString())
            .add("-f", FilenameUtils.getExtension(in))
            .add("-i", in)
            .add("-vn")
            .add("-acodec", "libvorbis")
            .add("-ab", String.valueOf(configuration.getBitrate()))
            .add("-ac", String.valueOf(configuration.getChannels()))
            .add("-ar", String.valueOf(configuration.getSamplingRate()))
            .add("-vol", String.valueOf(configuration.getVolume()))
            .add("-ss", String.valueOf(configuration.getStartTime()))
            .add("-f", FilenameUtils.getExtension(output.toString()))
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
  public void extract() {
    arguments.add("-y");
    arguments.add(output.toString());
    final ProcessBuilder builder = new ProcessBuilder(arguments);
    builder.redirectErrorStream(true);
    try {
      final Process p = builder.start();
      Logger.info(String.format("Starting Extraction Process with Arguments: %s", arguments));
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
    Logger.info("Finished Extraction Process..!");
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
