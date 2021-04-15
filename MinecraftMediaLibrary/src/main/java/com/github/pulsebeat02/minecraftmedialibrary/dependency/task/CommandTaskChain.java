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

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Constructs a chain of commands to be executed accordingly. */
public class CommandTaskChain {

  private final Map<CommandTask, Boolean> chain;

  /** Instantiates a new CommandTaskChain */
  public CommandTaskChain() {
    chain = new LinkedHashMap<>();
  }

  /**
   * Gets the command chain.
   *
   * @return the chain
   */
  public Map<CommandTask, Boolean> getChain() {
    return chain;
  }

  /**
   * Runs a task to the chain (synchronously).
   *
   * @param task to be added
   * @return the CommandTaskChain
   */
  public CommandTaskChain thenRun(@NotNull final CommandTask task) {
    chain.put(task, false);
    return this;
  }

  /**
   * Runs a task to the chain (asynchronously).
   *
   * @param task to be added
   * @return the CommandTaskChain
   */
  public CommandTaskChain thenRunAsync(@NotNull final CommandTask task) {
    chain.put(task, true);
    return this;
  }

  /**
   * Builds and runs the task chain.
   *
   * @throws IOException if an error occurred while receiving output
   */
  public void run() throws IOException {
    Logger.info(
        String.format("Command Chain Information (Thread: %d)", Thread.currentThread().getId()));
    for (final Map.Entry<CommandTask, Boolean> entry : chain.entrySet()) {
      Logger.info(String.join(" ", entry.getKey().getCommand()));
    }
    Logger.info("Running Command Chain... ");
    for (final Map.Entry<CommandTask, Boolean> entry : chain.entrySet()) {
      final CommandTask task = entry.getKey();
      if (entry.getValue()) {
        CompletableFuture.runAsync(
            () -> {
              try {
                task.run();
                Logger.info(
                    String.format(
                        "Task Command: %s Result: %s",
                        String.join(" ", task.getCommand()), task.getResult()));
              } catch (final IOException e) {
                e.printStackTrace();
              }
            });
      } else {
        task.run();
        try {
          if (task.getProcess().waitFor() == 0) {
            Logger.info(
                String.format(
                    "Task Command: %s Result: %s",
                    String.join(" ", task.getCommand()), task.getResult()));
          } else {
            Logger.info("An exception has occurred!");
          }
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
