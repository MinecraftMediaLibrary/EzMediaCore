package io.github.pulsebeat02.ezmediacore.callback.rewrite.http;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import org.jetbrains.annotations.NotNull;

public final class FFmpegHttpServerCallback extends ServerCallback {

  FFmpegHttpServerCallback(@NotNull final MediaLibraryCore core) {
    super(core);
  }

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {


  }

  @Override
  public void process(final byte @NotNull [] data) {}
}
