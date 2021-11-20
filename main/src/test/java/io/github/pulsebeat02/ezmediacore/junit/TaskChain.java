package io.github.pulsebeat02.ezmediacore.junit;

import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.pulsebeat02.ezmediacore.task.CommandTask;
import io.github.pulsebeat02.ezmediacore.task.CommandTaskChain;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public final class TaskChain {

  private static final String[] CMDS;

  static {
    CMDS = new String[]{"echo \"test\"", "echo \"hello from emc\""};
  }

  @Test
  public void executeCommand() throws IOException {
    final CommandTask task = new CommandTask(CMDS[0]);
    task.run();
    assertFalse(task.getOutput().isEmpty());
  }

  @Test
  public void executeChainCommand() throws IOException, InterruptedException {
    final CommandTaskChain chain = new CommandTaskChain()
        .thenRun(new CommandTask(CMDS[0]))
        .thenRunAsync(new CommandTask(CMDS[1]));
    chain.run();
  }

}
