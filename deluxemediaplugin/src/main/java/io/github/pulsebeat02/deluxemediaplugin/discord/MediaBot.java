package io.github.pulsebeat02.deluxemediaplugin.discord;

import io.github.pulsebeat02.deluxemediaplugin.discord.audio.MusicManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.core.Logger;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;

public class MediaBot {

  private final JDA jda;
  private final JDABuilder jdaBuilder;
  private final MusicManager musicManager;

  public MediaBot(String token) throws LoginException, InterruptedException {
    jdaBuilder = JDABuilder.createDefault(token);

    jdaBuilder.setStatus(OnlineStatus.ONLINE);
    jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
    jdaBuilder.enableIntents(
        GatewayIntent.GUILD_MEMBERS,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MESSAGE_REACTIONS);

    jda = jdaBuilder.build().awaitReady();

    musicManager = new MusicManager();
  }

  public @NotNull JDA getJda() {
    return this.jda;
  }

  public @NotNull JDABuilder getJdaBuilder() {
    return this.jdaBuilder;
  }

  public @NotNull MusicManager getMusicManager() {
    return this.musicManager;
  }
}
