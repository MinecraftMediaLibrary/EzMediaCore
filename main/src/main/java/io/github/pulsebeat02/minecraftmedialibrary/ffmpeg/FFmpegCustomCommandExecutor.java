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

import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * An FFmpeg command executor helper which allows users to freely use the FFmpeg process to what
 * they want.
 */
public class FFmpegCustomCommandExecutor implements CustomCommandExecutor {

  private final List<String> arguments;

  /** Instantiates a new FFmpegCustomCommandExecutor. */
  public FFmpegCustomCommandExecutor() {
    arguments = new ArrayList<>();
    arguments.add(FFmpegDependencyInstallation.getFFmpegPath().toString());
  }

  @Override
  public CustomCommandExecutor addArgument(@NotNull final String arg) {
    arguments.add(arg);
    return this;
  }

  @Override
  public CustomCommandExecutor addArguments(
      @NotNull final String key, @NotNull final String value) {
    arguments.add(key);
    arguments.add(value);
    return this;
  }

  @Override
  public CustomCommandExecutor addArgument(@NotNull final String arg, final int index) {
    arguments.add(index, arg);
    return this;
  }

  @Override
  public CustomCommandExecutor addArguments(
      @NotNull final String key, @NotNull final String value, final int index) {
    if (index < 0 || index > arguments.size() - 1) {
      return this;
    }
    arguments.add(index, value);
    arguments.add(index, key);
    return this;
  }

  @Override
  public CustomCommandExecutor removeArgument(@NotNull final String arg) {
    arguments.removeIf(next -> next.equals(arg));
    return this;
  }

  @Override
  public CustomCommandExecutor removeArgument(final int index) {
    arguments.remove(index);
    return this;
  }

  @Override
  public void execute() {
    execute(null);
  }

  @Override
  public void executeWithConsumer(@NotNull final Consumer<String> consumer) {
    execute(consumer);
  }

  @Override
  public List<String> getArguments() {
    return arguments;
  }

  @Override
  public void clearArguments() {
    arguments.clear();
  }

  /**
   * Internal implementation of execute which runs the command.
   *
   * @param consumer the consumer
   */
  private void execute(@Nullable final Consumer<String> consumer) {
    final boolean consume = consumer != null;
    final ProcessBuilder builder = new ProcessBuilder(arguments);
    builder.redirectErrorStream(true);
    try {
      final Process p = builder.start();
      try (final BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line;
        while (true) {
          line = r.readLine();
          if (line == null) {
            break;
          }
          if (consume) {
            consumer.accept(line);
          } else {
            Logger.info(line);
          }
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
