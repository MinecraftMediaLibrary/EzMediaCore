package io.github.pulsebeat02.deluxemediaplugin.discord.command;

import io.github.pulsebeat02.deluxemediaplugin.discord.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.discord.audio.MusicManager;
import java.util.Set;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConnectAudioCommand extends DiscordBaseCommand {

  public ConnectAudioCommand(@NotNull final MediaBot bot) {
    super(bot, "connect", Set.of());
  }

  @Override
  public boolean execute(@NotNull final Message executor, final String @Nullable [] arguments) {
    final MusicManager manager = this.getBot().getMusicManager();
    manager.joinVoiceChannel();
    executor
        .getChannel()
        .sendMessageEmbeds(
            new EmbedBuilder()
                .setTitle("Audio Voice Channel Connection")
                .setDescription("Connected to voice channel!")
                .build())
        .queue();
    return true;
  }
}
