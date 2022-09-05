package io.github.pulsebeat02.ezmediacore.callback.rewrite.discord;

import com.sun.jna.Pointer;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.external.VLCMediaPlayer;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.player.base.callback.AudioCallbackAdapter;

public final class VLCDiscordCallback extends DiscordCallback {

  VLCDiscordCallback(@NotNull final MediaLibraryCore core) {
    super(core);
  }

  @Override
  public void preparePlayerStateChange(
          @NotNull final VideoPlayer<?> player, @NotNull final PlayerControls status) {
    final VLCMediaPlayer vlc = (VLCMediaPlayer) player;

  }

  @Override
  public void process(final byte @NotNull [] data) {}

  private class MinecraftAudioCallback extends AudioCallbackAdapter {
    private final int blockSize;

    MinecraftAudioCallback(final int blockSize) {
      this.blockSize = blockSize;
    }

    @Override
    public void play(
        @NotNull final uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer,
        @NotNull final Pointer samples,
        final int sampleCount,
        final long pts) {
      final byte[] arr = samples.getByteArray(0, sampleCount * this.blockSize);
      VLCDiscordCallback.this.process(arr);
    }

    public int getBlockSize() {
      return this.blockSize;
    }
  }
}
