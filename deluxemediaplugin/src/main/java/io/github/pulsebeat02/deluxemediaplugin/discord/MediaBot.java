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
