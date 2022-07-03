package io.github.pulsebeat02.ezmediacore.callback.audio;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.AudioCallback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import org.jetbrains.annotations.NotNull;

public abstract class SampleCallback implements AudioCallback {

  private final MediaLibraryCore core;
  private final Viewers viewers;

  public SampleCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers) {
    checkNotNull(core, "MediaLibraryCore cannot be null!");
    checkNotNull(viewers, "Viewers cannot be null!");
    this.core = core;
    this.viewers = viewers;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public @NotNull Viewers getWatchers() {
    return this.viewers;
  }

  @Override
  public void preparePlayerStateChange(@NotNull final PlayerControls status) {
  }
}

