package io.github.pulsebeat02.ezmediacore.callback.audio;

import org.jetbrains.annotations.Nullable;

public interface FFmpegJDADiscordCallback {

  @Nullable
  JDAAudioStream getStream();
}
