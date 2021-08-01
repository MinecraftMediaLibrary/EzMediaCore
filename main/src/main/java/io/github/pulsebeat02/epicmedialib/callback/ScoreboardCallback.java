package io.github.pulsebeat02.epicmedialib.callback;

import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import io.github.pulsebeat02.epicmedialib.utility.ImmutableDimension;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class ScoreboardCallback extends FrameCallback implements ScoreboardCallbackDispatcher {

  private static ChatColor[] COLORS;

  static {
    try {
      final Field field = ChatColor.class.getDeclaredField("BY_CHAR");
      field.setAccessible(true);
      COLORS = ((Map<Character, ChatColor>) field.get(null)).values().toArray(new ChatColor[0]);
      field.setAccessible(false);
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private final Set<Player> viewers;
  private final String name;
  private Scoreboard scoreboard;
  private int id;

  public ScoreboardCallback(
      @NotNull final MediaLibraryCore core,
      final UUID[] viewers,
      final int id,
      @NotNull final ImmutableDimension dimension,
      final int blockWidth,
      final int delay) {
    super(core, viewers, dimension, blockWidth, delay);
    this.viewers = Collections.newSetFromMap(new WeakHashMap<>());
    this.viewers.addAll(Arrays.stream(viewers).map(Bukkit::getPlayer).collect(Collectors.toSet()));
    this.name = String.format("%s Video Player (%s)", core.getPlugin().getName(), id);
  }

  @Override
  public void process(final int[] data) {
    final long time = System.currentTimeMillis();
    if (time - getLastUpdated() >= getFrameDelay()) {
      setLastUpdated(time);
      final ImmutableDimension dimension = getDimensions();
      final int width = dimension.getWidth();
      final int height = dimension.getHeight();
      if (this.scoreboard == null) {
        this.scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        final Objective objective =
            this.scoreboard.registerNewObjective("rd-" + this.id++, "dummy", this.name);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 0; i < height; i++) {
          final Team team = this.scoreboard.registerNewTeam("SLOT_" + i);
          final String entry = COLORS[i].toString();
          team.addEntry(entry);
          objective.getScore(entry).setScore(15 - i);
        }
      }
      for (final Player player : this.viewers) {
        player.setScoreboard(this.scoreboard);
      }
      for (int y = 0; y < height; ++y) {
        int before = -1;
        final StringBuilder msg = new StringBuilder();
        for (int x = 0; x < width; ++x) {
          final int rgb = data[width * y + x];
          if (before != rgb) {
            msg.append(ChatColor.of("#" + String.format("%08x", rgb).substring(2)));
          }
          msg.append("\u2588");
          before = rgb;
        }
        for (final Player player : this.viewers) {
          player.sendMessage(msg.toString());
        }
        final Team team = this.scoreboard.getTeam("SLOT_" + y);
        if (team != null) {
          team.setSuffix(msg.toString());
        }
      }
    }
  }

  @Override
  public @NotNull String getScoreboardName() {
    return this.name;
  }

  @Override
  public int getScoreboardId() {
    return 0;
  }
}
