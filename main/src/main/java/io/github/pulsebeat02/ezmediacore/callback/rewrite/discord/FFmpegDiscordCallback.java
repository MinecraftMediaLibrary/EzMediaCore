package io.github.pulsebeat02.ezmediacore.callback.rewrite.discord;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.buffered.FFmpegMediaPlayer;
import org.jetbrains.annotations.NotNull;

public final class FFmpegDiscordCallback extends DiscordCallback {

  FFmpegDiscordCallback(@NotNull final MediaLibraryCore core) {
    super(core);
  }

  @Override
  public void preparePlayerStateChange(@NotNull final VideoPlayer player,
      @NotNull final PlayerControls status) {
    final FFmpegMediaPlayer ffmpeg = (FFmpegMediaPlayer) player;


  }

  @Override
  public void process(final byte @NotNull [] data) {

  }
}
