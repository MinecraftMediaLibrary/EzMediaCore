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
    CompletableFuture.runAsync(
        () -> {
          final String link = "%s/stream.m3u8".formatted(this.openFFmpegStream(plugin, mrl));
          final MediaBot bot = plugin.getMediaBot();
          final MusicManager manager = bot.getMusicManager();
          manager.destroyTrack();
          manager.joinVoiceChannel();
          try {
            TimeUnit.SECONDS.sleep(3L);
          } catch (final InterruptedException e) { // hack to wait for server start
            e.printStackTrace();
          }
          manager.addTrack(link);
          audience.sendMessage(Locale.DISCORD_AUDIO_STREAM.build());
        });
  }

  @Override
  public void setProperAudioHandler(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final VideoCommandAttributes attributes) {}
}
