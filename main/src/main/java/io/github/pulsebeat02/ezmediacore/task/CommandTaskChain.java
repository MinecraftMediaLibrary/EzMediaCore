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

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

/**
 * Constructs a chain of commands to be executed accordingly.
 */
public class CommandTaskChain {

  private final Map<CommandTask, Boolean> chain;

  /**
   * Instantiates a new CommandTaskChain
   */
  public CommandTaskChain() {
    this.chain = new LinkedHashMap<>();
  }

  /**
   * Gets the command chain.
   *
   * @return the chain
   */
  public Map<CommandTask, Boolean> getChain() {
    return this.chain;
  }

  /**
   * Runs a task to the chain (synchronously).
   *
   * @param task to be added
   * @return the CommandTaskChain
   */
  public CommandTaskChain thenRun(@NotNull final CommandTask task) {
    this.chain.put(task, false);
    return this;
  }

  /**
   * Runs a task to the chain (asynchronously).
   *
   * @param task to be added
   * @return the CommandTaskChain
   */
  public CommandTaskChain thenRunAsync(@NotNull final CommandTask task) {
    this.chain.put(task, true);
    return this;
  }

  /**
   * Builds and runs the task chain.
   *
   * @throws IOException if an error occurred while receiving output
   */
  public void run() throws IOException, InterruptedException {
    Logger.info("Command Chain Information (Thread: %d)".formatted(Thread.currentThread().getId()));
    for (final Map.Entry<CommandTask, Boolean> entry : this.chain.entrySet()) {
      Logger.info(String.join(" ", entry.getKey().getCommand()));
    }
    Logger.info("Running Command Chain... ");
    for (final Map.Entry<CommandTask, Boolean> entry : this.chain.entrySet()) {
      final CommandTask task = entry.getKey();
      if (entry.getValue()) {
        CompletableFuture.runAsync(
            () -> {
              try {
                task.run();
                Logger.info(
                    "Task Command: %s Result: %s"
                        .formatted(String.join(" ", task.getCommand()), task.getResult()));
              } catch (final IOException e) {
                e.printStackTrace();
              }
            },
            ExecutorProvider.EXTERNAL_PROCESS_POOL);
      } else {
        task.run();
        if (task.getProcess().waitFor() == 0) {
          Logger.info(
              "Task Command: %s Result: %s"
                  .formatted(String.join(" ", task.getCommand()), task.getResult()));
        } else {
          Logger.info("An exception has occurred!");
        }
      }
    }
  }
}
