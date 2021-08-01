package io.github.pulsebeat02.epicmedialib.callback;

import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import io.github.pulsebeat02.epicmedialib.utility.ImmutableDimension;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
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
      final UUID[] viewers,
      @NotNull final String character,
      @NotNull final ImmutableDimension dimension,
      final int blockWidth,
      final int delay) {
    super(core, viewers, dimension, blockWidth, delay);
    this.character = character;
    this.players = Collections.newSetFromMap(new WeakHashMap<>());
    this.players.addAll(
        Arrays.stream(getViewers()).map(Bukkit::getPlayer).collect(Collectors.toList()));
  }

  @Override
  public void process(final int[] data) {
    final long time = System.currentTimeMillis();
    if (time - getLastUpdated() >= getFrameDelay()) {
      setLastUpdated(time);
      final ImmutableDimension dimension = getDimensions();
      final int width = dimension.getWidth();
      final int height = dimension.getHeight();
      for (int y = 0; y < height; ++y) {
        int before = -1;
        final StringBuilder msg = new StringBuilder();
        for (int x = 0; x < width; ++x) {
          final int rgb = data[width * y + x];
          if (before != rgb) {
            msg.append(ChatColor.of("#" + String.format("%08x", rgb).substring(2)));
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
