package io.github.pulsebeat02.deluxemediaplugin.discord.command;

import io.github.pulsebeat02.deluxemediaplugin.discord.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.discord.audio.MusicManager;
import java.util.Set;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StopAudioCommand extends DiscordBaseCommand {

  public StopAudioCommand(@NotNull final MediaBot bot) {
    super(bot, "stop", Set.of());
  }

  @Override
  public boolean execute(@NotNull final Message executor, final String @Nullable [] arguments) {
    final MusicManager manager = this.getBot().getMusicManager();
    manager.getPlayerManager().shutdown();
    executor
        .getChannel()
        .sendMessageEmbeds(
            new EmbedBuilder().setTitle("Audio Stop").setDescription("Stopped Audio!").build())
        .queue();
    return true;
  }
}
