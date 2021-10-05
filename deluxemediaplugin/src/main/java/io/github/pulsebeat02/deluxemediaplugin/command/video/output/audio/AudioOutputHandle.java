package io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.StringKey;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public interface AudioOutputHandle extends StringKey {

  void setAudioHandler(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull VideoCommandAttributes attributes,
      @NotNull Audience audience,
      @NotNull final String mrl);

  void setProperAudioHandler(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final VideoCommandAttributes attributes);
}
