package io.github.pulsebeat02.ezmediacore.callback.audio;

import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioInputStream;

public interface FFmpegDiscordCallbackHandle {

  @Nullable
  AudioInputStream getStream();
}
