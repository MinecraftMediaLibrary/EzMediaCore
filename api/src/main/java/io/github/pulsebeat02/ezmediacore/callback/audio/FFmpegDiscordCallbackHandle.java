package io.github.pulsebeat02.ezmediacore.callback.audio;

import org.jetbrains.annotations.Nullable;

public interface FFmpegDiscordCallbackHandle {

  @Nullable
  JDAAudioPlayerStreamHandle getStream();
}
