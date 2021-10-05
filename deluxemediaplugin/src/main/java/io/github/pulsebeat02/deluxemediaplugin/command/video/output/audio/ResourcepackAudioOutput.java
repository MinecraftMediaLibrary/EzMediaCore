package io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommandAttributes;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import java.util.Set;
import net.kyori.adventure.audience.Audience;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResourcepackAudioOutput extends AudioOutput {

  public ResourcepackAudioOutput() {
    super("RESOURCEPACK");
  }

  @Override
  public void setAudioHandler(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final VideoCommandAttributes attributes,
      @NotNull final Audience audience,
      @NotNull final String mrl) {}

  @Override
  public void setProperAudioHandler(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final VideoCommandAttributes attributes) {
    final VideoPlayer player = attributes.getPlayer();
    final Set<Player> viewers = player.getWatchers().getPlayers();
    final String sound = player.getSoundKey().getName();
    for (final Player p : viewers) {
      p.playSound(p.getLocation(), sound, SoundCategory.MASTER, 100.0F, 1.0F);
    }
  }
}
