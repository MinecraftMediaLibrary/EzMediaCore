package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.dimension.ImmutableDimension;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatCallback extends FrameCallback implements ChatCallbackDispatcher {

  private final Set<Player> players;
  private final String character;

  public ChatCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final ImmutableDimension dimension,
      @NotNull final Collection<? extends Player> viewers,
      @NotNull final String character,
      final int blockWidth,
      final int delay) {
    super(core, dimension, viewers, blockWidth, delay);
    this.character = character;
    this.players = Collections.newSetFromMap(new WeakHashMap<>());
    this.players.addAll(
        Arrays.stream(this.getViewers()).map(Bukkit::getPlayer).collect(Collectors.toList()));
  }

  @Override
  public void process(final int[] data) {
    final long time = System.currentTimeMillis();
    if (time - this.getLastUpdated() >= this.getFrameDelay()) {
      this.setLastUpdated(time);
      final ImmutableDimension dimension = this.getDimensions();
      final int width = dimension.getWidth();
      final int height = dimension.getHeight();
      for (int y = 0; y < height; ++y) {
        int before = -1;
        final StringBuilder msg = new StringBuilder();
        for (int x = 0; x < width; ++x) {
          final int rgb = data[width * y + x];
          if (before != rgb) {
            msg.append(ChatColor.of("#" + "%08x".formatted(rgb).substring(2)));
          }
          msg.append(this.character);
          before = rgb;
        }
        for (final Player player : this.players) {
          player.sendMessage(msg.toString());
        }
      }
    }
  }

  @Override
  public @NotNull String getChatCharacter() {
    return this.character;
  }
}
