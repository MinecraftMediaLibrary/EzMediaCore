package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import org.jetbrains.annotations.NotNull;

public abstract sealed class AudioCallbackBuilder
    permits FFmpegDiscordCallback.Builder, ServerCallback.Builder, VLCDiscordCallback.Builder {

  public abstract @NotNull AudioOutput build(@NotNull final MediaLibraryCore core);
}
