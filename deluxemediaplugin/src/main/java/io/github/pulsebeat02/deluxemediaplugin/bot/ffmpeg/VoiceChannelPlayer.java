package io.github.pulsebeat02.deluxemediaplugin.bot.ffmpeg;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.executors.FixedExecutors;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.utility.concurrency.ThrowingConsumer;
import io.github.pulsebeat02.ezmediacore.utility.concurrency.ThrowingRunnable;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;
import javax.sound.sampled.AudioInputStream;

import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class VoiceChannelPlayer {

  private final DeluxeMediaPlugin plugin;
  private final Guild guild;

  public VoiceChannelPlayer(@NotNull final DeluxeMediaPlugin plugin, @NotNull final Guild guild) {
    this.plugin = plugin;
    this.guild = guild;
  }

  public void start(@NotNull final VoiceChannel channel, @NotNull final String input) {
    final CompletableFuture<Void> future = CompletableFuture.runAsync(() -> this.handleInput(channel, input));
    future.handle(Throwing.THROWING_FUTURE);
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
    player.play();
    manager.setSendingHandler(player);
  }

  private @NotNull AudioInputStream getAudioInputStream(@NotNull final String input) {
    final MediaLibraryCore core = this.plugin.library();
    final FFmpegPipedOutput output = new FFmpegPipedOutput(core, input);
    output.executeAsync(FixedExecutors.STREAM_THREAD_EXECUTOR);
    this.setStream(output);
    this.sleep();
    return output.getInputStream();
  }

  private void sleep() {
    Try.sleep(TimeUnit.SECONDS, 1);
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
