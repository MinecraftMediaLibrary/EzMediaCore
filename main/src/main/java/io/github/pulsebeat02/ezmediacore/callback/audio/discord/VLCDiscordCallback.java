package io.github.pulsebeat02.ezmediacore.callback.audio.discord;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/*
 * VLCDiscordCallback assumes the use for a Discord bot. While it does
 * not directly support actual libraries for Discord bots (for example, JDA
 * or Discord4J) it does allow you to pass in a consumer which will be
 * sent data from VLC for the library to input. The data is in a byte
 * array and wav format.
 */
public final class VLCDiscordCallback extends DiscordCallback {

  private final Consumer<byte[]> audioConsumer;

  VLCDiscordCallback(
      @NotNull final MediaLibraryCore core, @NotNull final Consumer<byte[]> audioConsumer) {
    super(core);
    this.audioConsumer = audioConsumer;
  }

  @Override
  public void preparePlayerStateChange(
          @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {
  }

  @Override
  public void process(final byte @NotNull [] data) {
    this.audioConsumer.accept(data);
  }
}
