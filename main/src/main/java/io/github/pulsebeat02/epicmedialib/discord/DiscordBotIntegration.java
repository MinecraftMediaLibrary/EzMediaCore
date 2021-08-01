package io.github.pulsebeat02.epicmedialib.discord;

import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.utils.Compression;
import org.jetbrains.annotations.NotNull;

public class DiscordBotIntegration {

  private final String token;
  private JDA jda;

  public DiscordBotIntegration(@NotNull final String token) {
    this.token = token;
  }

  public void initialize() {
    try {
      jda =
          JDABuilder.createDefault(token)
              .setBulkDeleteSplittingEnabled(false)
              .setCompression(Compression.NONE)
              .setActivity(Activity.playing("Audio for Users"))
              .build();
    } catch (final LoginException e) {
      e.printStackTrace();
    }
  }

  public void connect(final long id) {
    final VoiceChannel channel = jda.getVoiceChannelById(id);
  }
}
