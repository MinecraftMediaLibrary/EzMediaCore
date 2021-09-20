package io.github.pulsebeat02.deluxemediaplugin.discord.command;

import io.github.pulsebeat02.deluxemediaplugin.discord.MediaBot;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public abstract class DiscordBaseCommand {

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

  public abstract boolean execute(final @NotNull String[] arguments);

  public @NotNull String getCommand() {
    return this.command;
  }

  public @NotNull Collection<DiscordBaseCommand> getArguments() {
    return this.subcommands;
  }

  public @NotNull MediaBot getBot() {
    return this.bot;
  }
}
