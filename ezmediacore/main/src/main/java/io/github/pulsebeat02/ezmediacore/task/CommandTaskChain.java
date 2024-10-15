/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/** Constructs a chain of commands to be executed accordingly. */
public class CommandTaskChain {

  private static final ExecutorService EXTERNAL_PROCESS_POOL = Executors.newVirtualThreadPerTaskExecutor();

  private final Map<CommandTask, Boolean> chain;

  /** Instantiates a new CommandTaskChain */
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
  public CommandTaskChain thenRun( final CommandTask task) {
    this.chain.put(task, false);
    return this;
  }

  /**
   * Runs a task to the chain (asynchronously).
   *
   * @param task to be added
   * @return the CommandTaskChain
   */
  public CommandTaskChain thenRunAsync( final CommandTask task) {
    this.chain.put(task, true);
    return this;
  }

  /**
   * Builds and runs the task chain.
   *
   * @throws IOException if an error occurred while receiving output
   */
  public void run() throws IOException, InterruptedException {
    this.runInternalChain();
  }

  private void runInternalChain() throws IOException, InterruptedException {
    final Set<Entry<CommandTask, Boolean>> entrySet = this.chain.entrySet();
    for (final Map.Entry<CommandTask, Boolean> entry : entrySet) {
      final CommandTask task = entry.getKey();
      this.handleCommandTask(entry, task);
    }
  }

  private void handleCommandTask(
       final Entry<CommandTask, Boolean> entry,  final CommandTask task)
      throws IOException, InterruptedException {
    if (this.isAsync(entry)) {
      this.runAsync(task);
    } else {
      this.runTaskChain(task);
    }
  }

  private void runAsync( final CommandTask task) {
    CompletableFuture.runAsync(this.runSeparateTask(task), EXTERNAL_PROCESS_POOL);
  }

  private boolean isAsync( final Map.Entry<CommandTask, Boolean> entry) {
    return entry.getValue();
  }

  private  Runnable runSeparateTask( final CommandTask task) {
    return () -> this.runTask(task);
  }

  private void runTask( final CommandTask task) {
    try {
      task.run();
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private String getTaskMessage( final CommandTask task) throws IOException {
    final String[] cmd = task.getCommand();
    final String output = task.getOutput();
    final String joined = String.join(" ", cmd);
    return "Task Command: %s Result: %s"
        .formatted(joined, output);
  }

  private void runTaskChain( final CommandTask task)
      throws IOException, InterruptedException {

    task.run();

    final Process process = task.getProcess();
    final int code = process.waitFor();
    if (code != 0) {
      throw new IOException("Error occurred while running program!");
    }
  }
}
