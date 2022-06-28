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
package io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.bot.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.bot.ffmpeg.VoiceChannelPlayer;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.MrlInput;
import io.github.pulsebeat02.ezmediacore.request.MediaRequest;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DiscordAudioOutput extends FFmpegOutput {

  public DiscordAudioOutput() {
    super("DISCORD");
  }

  @Override
  public void setAudioHandler(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final ScreenConfig attributes,
      @NotNull final Audience audience,
      @NotNull final String mrl) {
    CompletableFuture.runAsync(() -> this.handleAudio(plugin, audience, mrl))
        .handle(Throwing.THROWING_FUTURE);
  }

  private void handleAudio(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final Audience audience,
      @NotNull final String mrl) {

    final MediaRequest info = RequestUtils.requestMediaInformation(MrlInput.ofMrl(mrl));
    final Input input = info.getAudioLinks().get(0);
    final String raw = input.getInput();

    final MediaBot bot = plugin.getMediaBot();
    final Guild guild = bot.getGuild();
    final VoiceChannel channel = bot.getChannel();

    final VoiceChannelPlayer player = new VoiceChannelPlayer(plugin, guild);
    player.start(channel, raw);

    audience.sendMessage(Locale.DISCORD_AUDIO_STREAM.build());
  }

  @Override
  public void setProperAudioHandler(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig attributes) {}
}
