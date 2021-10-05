package io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import org.jetbrains.annotations.NotNull;

public interface FFmpegOutputHandle {

  @NotNull
  String openFFmpegStream(@NotNull final DeluxeMediaPlugin plugin, @NotNull final String mrl);
}
