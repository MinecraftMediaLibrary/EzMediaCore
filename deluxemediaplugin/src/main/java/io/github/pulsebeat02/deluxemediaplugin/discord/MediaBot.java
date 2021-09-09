package io.github.pulsebeat02.deluxemediaplugin.discord;

import io.github.pulsebeat02.deluxemediaplugin.discord.audio.MusicManager;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;

public class MediaBot {

  private final JDA jda;
  private final JDABuilder jdaBuilder;
  private final MusicManager musicManager;

  public MediaBot(@NotNull final String token) throws LoginException, InterruptedException {
    this.jdaBuilder = JDABuilder.createDefault(token);
    this.jdaBuilder.setStatus(OnlineStatus.ONLINE);
    this.jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
    this.jdaBuilder.enableIntents(
        GatewayIntent.GUILD_MEMBERS,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MESSAGE_REACTIONS);
    this.jda = this.jdaBuilder.build().awaitReady();
    this.musicManager = new MusicManager();
  }

  public @NotNull JDA getJDA() {
    return this.jda;
  }

  public @NotNull JDABuilder getJDABuilder() {
    return this.jdaBuilder;
  }

  public @NotNull MusicManager getMusicManager() {
    return this.musicManager;
  }
}
