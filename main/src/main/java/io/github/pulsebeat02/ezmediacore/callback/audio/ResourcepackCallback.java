package io.github.pulsebeat02.ezmediacore.callback.audio;

import static org.bukkit.SoundCategory.MASTER;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.AudioCallback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ResourcepackCallback extends SampleCallback {

  private final SoundKey key;

  ResourcepackCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers,
      @Nullable final SoundKey key) {
    super(core, viewers);
    this.key = this.getInternalSoundKey(key);
  }

  private @NotNull SoundKey getInternalSoundKey(@Nullable final SoundKey key) {
    if (key == null) {
      return SoundKey.ofSound(
          this.getCore().getPlugin().getName().toLowerCase(java.util.Locale.ROOT));
    }
    return key;
  }


  @Override
  public void process(final byte @NotNull [] data) {
  }

  @Override
  public void preparePlayerStateChange(@NotNull final PlayerControls status) {
    if (status == PlayerControls.START || status == PlayerControls.RESUME) {
      this.playAudio();
    } else {
      this.stopAudio();
    }
  }

  public void stopAudio() {
    final Viewers viewers = this.getWatchers();
    for (final Player player : viewers.getPlayers()) {
      player.stopSound(this.key.getName(), MASTER);
    }
  }

  private void playAudio() {
    final Viewers viewers = this.getWatchers();
    for (final Player player : viewers.getPlayers()) {
      player.playSound(player.getLocation(), this.key.getName(), MASTER, 1.0f, 1.0f);
    }
  }

  public static final class Builder extends AudioCallbackBuilder {

    private SoundKey key;

    {
      this.key = SoundKey.ofSound("emc");
    }

    public Builder() {}

    @Contract("_ -> this")
    public @NotNull Builder key(@NotNull final SoundKey key) {
      this.key = key;
      return this;
    }


    @Override
    public @NotNull AudioCallback build(@NotNull final MediaLibraryCore core) {
      final Viewers viewers = this.getViewers();
      return new ResourcepackCallback(core, viewers, this.key);
    }
  }
}
