package io.github.pulsebeat02.deluxemediaplugin.command.video.output.video;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.ezmediacore.callback.CallbackBuilder;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoBuilder;
import java.util.Collection;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ScoreboardOutput extends VideoOutput {

  public ScoreboardOutput() {
    super("SCOREBOARD");
  }

  @Override
  public boolean createVideoPlayer(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final VideoCommandAttributes attributes,
      @NotNull final CommandSender sender,
      @NotNull final Collection<? extends Player> players) {
    if (!(sender instanceof final Player player)) {
      plugin.audience().sender(sender).sendMessage(Locale.ERR_PLAYER_SENDER.build());
      return false;
    }
    attributes.setPlayer(
        VideoBuilder.unspecified()
            .callback(
                CallbackBuilder.blockHighlight()
                    .location(player.getLocation())
                    .dims(
                        Dimension.ofDimension(
                            attributes.getPixelWidth(), attributes.getPixelHeight()))
                    .delay(DelayConfiguration.ofDelay(40))
                    .build(plugin.library()))
            .soundKey(SoundKey.ofSound("emc"))
            .build());
    return true;
  }
}
