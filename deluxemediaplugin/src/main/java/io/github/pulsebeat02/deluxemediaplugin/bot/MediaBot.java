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

package io.github.pulsebeat02.deluxemediaplugin.bot;

import io.github.pulsebeat02.deluxemediaplugin.bot.audio.MusicManager;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;

public class MediaBot {

  private final JDA jda;
  private final Guild guild;
  private final VoiceChannel channel;
  private final MusicManager musicManager;
  private String prefix;

  public MediaBot(
      @NotNull final String token, @NotNull final String guild, @NotNull final String voicechannel)
      throws LoginException, InterruptedException {
    this.jda =
        JDABuilder.createDefault(token)
            .setStatus(OnlineStatus.ONLINE)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS)
            .setEventManager(new AnnotatedEventManager())
            .addEventListeners(new MediaCommandListener(this))
            .build()
            .awaitReady();
    this.guild = this.jda.getGuildById(guild);
    this.channel = this.jda.getVoiceChannelById(voicechannel);
    this.musicManager = new MusicManager(this);
    this.prefix = "d!";
  }

  public @NotNull JDA getJDA() {
    return this.jda;
  }

  public @NotNull MusicManager getMusicManager() {
    return this.musicManager;
  }

  public @NotNull Guild getGuild() {
    return this.guild;
  }

  public @NotNull VoiceChannel getChannel() {
    return this.channel;
  }

  public @NotNull String getPrefix() {
    return this.prefix;
  }

  public void setPrefix(@NotNull final String prefix) {
    this.prefix = prefix;
  }
}
