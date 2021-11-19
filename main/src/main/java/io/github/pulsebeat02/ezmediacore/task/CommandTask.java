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

package io.github.pulsebeat02.ezmediacore.task;

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.jetbrains.annotations.NotNull;

/**
 * A specialized CommandTask which executes native commands from the Runtime. The class is used for
 * easier command execution as well as an easier way to "hold" onto commands and wait before
 * execution.
 */
public class CommandTask {

  private static final Runtime RUNTIME;

  static {
    RUNTIME = Runtime.getRuntime();
  }

  private final String[] command;
  private Process process;

  /**
   * Instantiates a CommandTask.
   *
   * @param command command
   * @param runOnCreation whether it should be ran instantly
   * @throws IOException if the command isn't valid (when ran instantly)
   */
  public CommandTask(@NotNull final String[] command, final boolean runOnCreation)
      throws IOException {
    this.command = command;
    if (runOnCreation) {
      this.run();
    }
  }

  /**
   * Instantiates a CommandTask.
   *
   * @param command command
   */
  public CommandTask(@NotNull final String... command) {
    this.command = command;
  }

  /**
   * Runs the specific command.
   *
   * @throws IOException if the command isn't valid
   */
  public void run() throws IOException {
    this.process = RUNTIME.exec(this.command);
    this.getOutput();
  }

  /**
   * Gets and assigns the output for the command
   *
   * @throws IOException if the output cannot be read
   */
  public @NotNull String getOutput() throws IOException {
    final StringBuilder output = new StringBuilder();
    final BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new FastBufferedInputStream(this.process.getInputStream())));
    String str;
    while ((str = br.readLine()) != null) {
      output.append(str);
    }
    br.close();
    return output.toString();
  }

  /**
   * Gets the command.
   *
   * @return array of command arguments
   */
  public String[] getCommand() {
    return this.command;
  }

  /**
   * Gets the process with this specific command.
   *
   * @return the process
   */
  public Process getProcess() {
    return this.process;
  }
}
