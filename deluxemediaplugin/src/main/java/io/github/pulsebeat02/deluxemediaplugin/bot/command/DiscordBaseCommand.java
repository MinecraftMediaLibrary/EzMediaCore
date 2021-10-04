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
package io.github.pulsebeat02.deluxemediaplugin.bot.command;

import io.github.pulsebeat02.deluxemediaplugin.bot.MediaBot;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public abstract class DiscordBaseCommand implements DiscordCommand {

  private final MediaBot bot;
  private final String command;
  private final Collection<DiscordBaseCommand> subcommands;

  public DiscordBaseCommand(
      @NotNull final MediaBot bot,
      @NotNull final String command,
      @NotNull final Collection<DiscordBaseCommand> subcommands) {
    this.bot = bot;
    this.command = command;
    this.subcommands = subcommands;
  }

  @Override
  public @NotNull String getCommand() {
    return this.command;
  }

  @Override
  public @NotNull Collection<DiscordBaseCommand> getArguments() {
    return this.subcommands;
  }

  @Override
  public @NotNull MediaBot getBot() {
    return this.bot;
  }
}
