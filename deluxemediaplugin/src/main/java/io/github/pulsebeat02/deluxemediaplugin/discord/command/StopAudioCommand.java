package io.github.pulsebeat02.deluxemediaplugin.discord.command;

import io.github.pulsebeat02.deluxemediaplugin.discord.MediaBot;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class StopAudioCommand extends DiscordBaseCommand {

  public StopAudioCommand(@NotNull final MediaBot bot) {
    super(bot, "stop", Set.of());
  }

  @Override
  public boolean execute(@NotNull final String[] arguments) {
    return false;
  }
}
