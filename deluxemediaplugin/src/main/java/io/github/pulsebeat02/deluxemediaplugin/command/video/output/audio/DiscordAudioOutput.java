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
import io.github.pulsebeat02.deluxemediaplugin.bot.audio.MusicManager;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public class DiscordAudioOutput extends FFmpegOutput {

  public DiscordAudioOutput() {
    super("DISCORD");
  }

  @Override
  public void setAudioHandler(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final VideoCommandAttributes attributes,
      @NotNull final Audience audience,
      @NotNull final String mrl) {
    CompletableFuture.runAsync(() -> this.handleAudio(plugin, audience, mrl));
  }

  private void handleAudio(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final Audience audience,
      @NotNull final String mrl) {

    final String link = "%s/stream.m3u8".formatted(this.openFFmpegStream(plugin, mrl));
    final MediaBot bot = plugin.getMediaBot();
    final MusicManager manager = this.getMusicManager(bot);

    this.sleep();

    manager.addTrack(link);

    audience.sendMessage(Locale.DISCORD_AUDIO_STREAM.build());
  }

  private void sleep() {
    try {
      TimeUnit.SECONDS.sleep(3L);
    } catch (final InterruptedException e) { // hack to wait for server start
      e.printStackTrace();
    }
  }

  @NotNull
  private MusicManager getMusicManager(@NotNull final MediaBot bot) {

    final MusicManager manager = bot.getMusicManager();
    manager.destroyTrack();
    manager.joinVoiceChannel();

    return manager;
  }

  @Override
  public void setProperAudioHandler(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final VideoCommandAttributes attributes) {}
}
