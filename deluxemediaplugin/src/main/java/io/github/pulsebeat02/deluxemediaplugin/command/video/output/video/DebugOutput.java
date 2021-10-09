package io.github.pulsebeat02.deluxemediaplugin.command.video.output.video;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import io.github.pulsebeat02.ezmediacore.callback.CallbackBuilder;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.Identifier;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoBuilder;
import java.util.Collection;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DebugOutput extends VideoOutput {

  public DebugOutput() {
    super("DEBUG_HIGHLIGHTS");
  }

  @Override
  public boolean createVideoPlayer(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final VideoCommandAttributes attributes,
      @NotNull final CommandSender sender,
      @NotNull final Collection<? extends Player> players) {
    attributes.setPlayer(
        VideoBuilder.unspecified()
            .callback(
                CallbackBuilder.scoreboard()
                    .id(Identifier.ofIdentifier(1080))
                    .dims(
                        Dimension.ofDimension(
                            attributes.getPixelWidth(), attributes.getPixelHeight()))
                    .viewers(Viewers.ofPlayers(players))
                    .delay(DelayConfiguration.DELAY_20_MS)
                    .build(plugin.library()))
            .soundKey(SoundKey.ofSound("emc"))
            .build());
    return true;
  }
}
