/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
package io.github.pulsebeat02.deluxemediaplugin.bot.ffmpeg;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.executors.FixedExecutors;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioInputStream;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

public final class VoiceChannelPlayer {

  private final DeluxeMediaPlugin plugin;
  private final Guild guild;

  public VoiceChannelPlayer(@NotNull final DeluxeMediaPlugin plugin, @NotNull final Guild guild) {
    this.plugin = plugin;
    this.guild = guild;
  }

  public void start(@NotNull final VoiceChannel channel, @NotNull final String input) {
    this.handleInput(channel, input);
  }

  private void handleInput(@NotNull final VoiceChannel channel, @NotNull final String input) {
    final AudioManager manager = this.guild.getAudioManager();
    this.closeAudioManager(manager);
    this.setSendingHandler(input, manager);
    this.openAudioManager(channel, manager);
  }

  private void setSendingHandler(@NotNull final String input, @NotNull final AudioManager manager) {
    final AudioInputStream stream = this.getAudioInputStream(input);
    final AudioPlayerStreamSendHandler player = new AudioPlayerStreamSendHandler(stream);
    this.setDiscordSendHandler(player);
    this.setManagerSendingHandler(manager, player);
  }

  private void setManagerSendingHandler(
      @NotNull final AudioManager manager, final AudioPlayerStreamSendHandler player) {
    manager.setSendingHandler(player);
  }

  private void setDiscordSendHandler(@NotNull final AudioPlayerStreamSendHandler player) {
    final ScreenConfig config = this.plugin.getScreenConfig();
    config.setDiscordHandler(player);
  }

  private @NotNull AudioInputStream getAudioInputStream(@NotNull final String input) {
    final FFmpegPipedOutput output = this.createPipedFFmpegOutput(input);
    this.setStream(output);
    this.sleep();
    return output.getInputStream();
  }

  @NotNull
  private FFmpegPipedOutput createPipedFFmpegOutput(@NotNull final String input) {
    final MediaLibraryCore core = this.plugin.library();
    final FFmpegPipedOutput output = new FFmpegPipedOutput(core, input);
    output.executeAsync(FixedExecutors.STREAM_THREAD_EXECUTOR);
    return output;
  }

  private void sleep() {
    Try.sleep(TimeUnit.SECONDS, 5);
  }

  private void setStream(@NotNull final FFmpegPipedOutput output) {
    final ScreenConfig config = this.plugin.getScreenConfig();
    config.setStream(output);
  }

  private void closeAudioManager(@NotNull final AudioManager manager) {
    if (manager.isConnected()) {
      manager.closeAudioConnection();
    }
  }

  private void openAudioManager(
      @NotNull final VoiceChannel channel, @NotNull final AudioManager manager) {
    manager.openAudioConnection(channel);
  }
}
