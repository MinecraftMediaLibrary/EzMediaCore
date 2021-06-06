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

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public interface CustomCommandExecutor {

  /**
   * Adds an argument to the command.
   *
   * @param arg the argument
   * @return the CustomCommandExecutor
   */
  CustomCommandExecutor addArgument(@NotNull final String arg);

  /**
   * Adds arguments (-key value or similar) to the command.
   *
   * @param key the key
   * @param value the value
   * @return the CustomCommandExecutor
   */
  CustomCommandExecutor addArguments(@NotNull final String key, @NotNull final String value);

  /**
   * Adds an argument to the command at the specified index.
   *
   * @param arg the argument
   * @param index the index
   * @return the CustomCommandExecutor
   */
  CustomCommandExecutor addArgument(@NotNull final String arg, final int index);

  /**
   * Adds arguments (-key value or similar) to the command at the specified index.
   *
   * @param key the key
   * @param value the value
   * @param index the index
   * @return the CustomCommandExecutor
   */
  CustomCommandExecutor addArguments(
      @NotNull final String key, @NotNull final String value, final int index);

  /**
   * Removes all traces of the argument from the command arguments.
   *
   * @param arg the argument
   * @return the CustomCommandExecutor
   */
  CustomCommandExecutor removeArgument(@NotNull final String arg);

  /**
   * Removes the argument at the specified index.
   *
   * @param index the index
   * @return the CustomCommandExecutor
   */
  CustomCommandExecutor removeArgument(final int index);

  /** Executes the command with arguments. */
  void execute();

  /**
   * Executes the command with arguments and a consumer (to log information).
   *
   * @param consumer the consumer
   */
  void executeWithConsumer(@NotNull final Consumer<String> consumer);

  /**
   * Gets the arguments of the command.
   *
   * @return the arguments
   */
  List<String> getArguments();
}
