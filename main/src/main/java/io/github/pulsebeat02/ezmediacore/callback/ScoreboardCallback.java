/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.utility.TaskUtils;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jcodec.codecs.mjpeg.tools.AssertionException;
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

  private final Collection<? extends Player> viewers;
  private final String name;
  private Scoreboard scoreboard;
  private int id;

  public ScoreboardCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Dimension dimension,
      @NotNull final Collection<? extends Player> viewers,
      final int id,
      final int blockWidth,
      final int delay) {
    super(core, dimension, viewers, blockWidth, delay);
    this.viewers = viewers;
    this.name = "%s Video Player (%s)".formatted(core.getPlugin().getName(), id);
  }

  @Override
  public void process(final int[] data) {
    TaskUtils.sync(this.getCore(), (Callable<Void>) () -> {
      final long time = System.currentTimeMillis();
      if (time - this.getLastUpdated() >= this.getFrameDelay()) {
        this.setLastUpdated(time);
        final Dimension dimension = this.getDimensions();
        final int width = dimension.getWidth();
        final int height = dimension.getHeight();
        if (this.scoreboard == null) {
          final ScoreboardManager manager = Bukkit.getScoreboardManager();
          if (manager == null) {
            throw new AssertionException("No worlds are loaded!");
          }
          this.scoreboard = manager.getNewScoreboard();
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
              msg.append(ChatColor.of("#" + "%08x".formatted(rgb).substring(2)));
            }
            msg.append("\u2588");
            before = rgb;
          }
          final Team team = this.scoreboard.getTeam("SLOT_" + y);
          if (team != null) {
            team.setSuffix(msg.toString());
          }
        }
      }
      return null;
    });
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
