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

import static net.dv8tion.jda.api.OnlineStatus.ONLINE;
import static net.dv8tion.jda.api.entities.Activity.playing;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGE_REACTIONS;

import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;

public final class MediaBot {

  private final JDA jda;
  private final Guild guild;
  private final VoiceChannel channel;
  private String prefix;

  public MediaBot(
      @NotNull final String token, @NotNull final String guild, @NotNull final String voicechannel)
      throws LoginException, InterruptedException, ErrorResponseException {
    this.jda = this.createBot(token);
    this.guild = this.jda.getGuildById(guild);
    this.channel = this.jda.getVoiceChannelById(voicechannel);
    this.prefix = "d!";
    this.setPresence();
  }

  @NotNull
  private JDA createBot(@NotNull final String token) throws InterruptedException, LoginException {
    return JDABuilder.createDefault(token)
        .setStatus(ONLINE)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .enableIntents(GUILD_MEMBERS, GUILD_MESSAGES, GUILD_MESSAGE_REACTIONS)
        .setEventManager(new AnnotatedEventManager())
        .addEventListeners(new MediaCommandListener(this))
        .build()
        .awaitReady();
  }

  private void setPresence() {
    this.jda.getPresence().setPresence(ONLINE, playing("DeluxeMediaPlugin Audio"));
  }

  public void joinVoiceChannel() {
    final AudioManager manager = this.guild.getAudioManager();
    manager.openAudioConnection(this.channel);
  }

  public void leaveVoiceChannel() {
    final AudioManager manager = this.guild.getAudioManager();
    manager.closeAudioConnection();
  }

  public @NotNull JDA getJDA() {
    return this.jda;
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
