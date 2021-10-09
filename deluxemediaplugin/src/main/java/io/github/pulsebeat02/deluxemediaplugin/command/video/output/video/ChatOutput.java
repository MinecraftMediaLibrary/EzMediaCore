package io.github.pulsebeat02.deluxemediaplugin.command.video.output.video;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import io.github.pulsebeat02.ezmediacore.callback.CallbackBuilder;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.entity.NamedEntityString;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoBuilder;
import java.util.Collection;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatOutput extends VideoOutput {

  public ChatOutput() {
    super("CHATBOX");
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
                CallbackBuilder.chat()
                    .character(NamedEntityString.NORMAL_SQUARE)
                    .dims(
                        Dimension.ofDimension(
                            attributes.getPixelWidth(), attributes.getPixelHeight()))
                    .viewers(Viewers.ofPlayers(players))
                    .delay(DelayConfiguration.ofDelay(20))
                    .build(plugin.library()))
            .soundKey(SoundKey.ofSound("emc"))
            .build());
    return true;
  }
}
