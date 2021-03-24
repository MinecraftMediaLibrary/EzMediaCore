/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/2/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency.task;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
  private String result;

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
      run();
    }
    getOutput();
  }

  /**
   * Runs the specific command.
   *
   * @throws IOException if the command isn't valid
   */
  public void run() throws IOException {
    process = RUNTIME.exec(command);
  }

  /**
   * Gets and assigns the output for the command
   *
   * @throws IOException if the output cannot be read
   */
  public void getOutput() throws IOException {
    final StringBuilder output = new StringBuilder();
    final BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String str;
    while ((str = br.readLine()) != null) {
      output.append(str);
    }
    br.close();
    result = output.toString();
  }

  /**
   * Gets the command.
   *
   * @return array of command arguments
   */
  public String[] getCommand() {
    return command;
  }

  /**
   * Gets the process with this specific command.
   *
   * @return the process
   */
  public Process getProcess() {
    return process;
  }

  /**
   * Gets result of the command.
   *
   * @return the result
   */
  public String getResult() {
    return result;
  }
}
