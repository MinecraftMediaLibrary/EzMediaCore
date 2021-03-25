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

package com.github.pulsebeat02.minecraftmedialibrary.dependency.task;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Constructs a chain of commands to be executed accordingly. */
public class CommandTaskChain {

  private final List<CommandTask> chain;

  /** Instantiates a new CommandTaskChain */
  public CommandTaskChain() {
    chain = new ArrayList<>();
  }

  /**
   * Gets the command chain.
   *
   * @return the chain
   */
  public List<CommandTask> getChain() {
    return chain;
  }

  /**
   * Adds a task to the chain.
   *
   * @param task to be added
   * @return the CommandTaskChain
   */
  public CommandTaskChain addTask(@NotNull final CommandTask task) {
    chain.add(task);
    return this;
  }

  /** Builds and runs the task chain. */
  public void build() {
    for (final CommandTask task : chain) {
      try {
        task.run();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }
}
