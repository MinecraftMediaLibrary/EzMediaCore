/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.utils.ffmpeg;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import me.brandonli.mcav.utils.runtime.CommandTask;

/**
 * An FFmpeg command line, built with {@link #builder()} and run through the task of {@link #createTask()}.
 *
 * {@snippet :
 * final String input = "input.mp4";
 * final String codec = "libvorbis";
 * final String output = "output.ogg";
 * final FfmpegCommandBuilder builder = FfmpegCommand.builder();
 * builder.addInput(input);
 * builder.addAudioCodec(codec);
 * builder.addOverwrite();
 * builder.addOutput(output);
 * final FfmpegCommand command = builder.build();
 * final CommandTask task = command.createTask();
 * final int exitCode = task.run();
 * }
 *
 * <p>This class is immutable and thread-safe.
 */
public final class FfmpegCommand {

  private static final String ARGUMENT_SEPARATOR = " ";

  private final List<String> arguments;

  FfmpegCommand(final List<String> arguments) {
    this.arguments = List.copyOf(arguments);
  }

  /**
   * Creates a builder for a command.
   *
   * @return the builder
   */
  public static FfmpegCommandBuilder builder() {
    return new DefaultFfmpegCommandBuilder();
  }

  /**
   * Returns the complete command line, starting with the executable.
   *
   * @return the command line as separate arguments
   * @throws UnsatisfiedLinkError if the bundled executable cannot be extracted for this platform
   */
  public String[] toCommandArray() {
    final Path executable = this.getExecutable();
    final String executablePath = executable.toString();
    final int argumentCount = this.arguments.size();
    final List<String> command = new ArrayList<>(argumentCount + 1);
    command.add(executablePath);
    command.addAll(this.arguments);
    return command.toArray(new String[0]);
  }

  /**
   * Creates a task for the command without starting it. Call {@link CommandTask#run()} or
   * {@link CommandTask#run(java.time.Duration)} to run it and wait for the exit code.
   *
   * @return the task, which has not been run yet
   * @throws UnsatisfiedLinkError if the bundled executable cannot be extracted for this platform
   */
  public CommandTask createTask() {
    final String[] command = this.toCommandArray();
    return new CommandTask(command);
  }

  /**
   * Returns the arguments after the executable.
   *
   * @return the arguments, which cannot be modified
   */
  public List<String> getArguments() {
    return this.arguments;
  }

  /**
   * Returns the FFmpeg executable the command runs with.
   *
   * @return the path of the executable
   * @throws UnsatisfiedLinkError if the bundled executable cannot be extracted for this platform
   */
  public Path getExecutable() {
    return FfmpegExecutableProvider.getFfmpegPath();
  }

  /**
   * Joins the executable and arguments with spaces for diagnostics. The result is not shell-escaped and should
   * not be parsed to execute the command; use {@link #toCommandArray()} instead.
   *
   * @return the diagnostic command text
   * @throws UnsatisfiedLinkError if the bundled executable cannot be extracted for this platform
   */
  @Override
  public String toString() {
    final String[] command = this.toCommandArray();
    return String.join(ARGUMENT_SEPARATOR, command);
  }
}
