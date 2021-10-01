package io.github.pulsebeat02.deluxemediaplugin.json;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class MediaAttributesData extends DataProvider<VideoCommandAttributes>  {

  public MediaAttributesData(
      @NotNull final DeluxeMediaPlugin plugin) throws IOException {
    super(plugin, "video-attributes.json");
  }
}
