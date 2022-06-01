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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.BLACK;
import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.BOLD;
import static org.bukkit.ChatColor.DARK_AQUA;
import static org.bukkit.ChatColor.DARK_BLUE;
import static org.bukkit.ChatColor.DARK_GRAY;
import static org.bukkit.ChatColor.DARK_GREEN;
import static org.bukkit.ChatColor.DARK_PURPLE;
import static org.bukkit.ChatColor.DARK_RED;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.ITALIC;
import static org.bukkit.ChatColor.LIGHT_PURPLE;
import static org.bukkit.ChatColor.MAGIC;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.RESET;
import static org.bukkit.ChatColor.STRIKETHROUGH;
import static org.bukkit.ChatColor.UNDERLINE;
import static org.bukkit.ChatColor.WHITE;
import static org.bukkit.ChatColor.YELLOW;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.implementation.ScoreboardCallbackDispatcher;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.utility.task.TaskUtils;
import java.nio.IntBuffer;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ScoreboardCallback extends FrameCallback implements ScoreboardCallbackDispatcher {

  private static final ChatColor[] COLORS;

  static {
    COLORS =
        new ChatColor[] {
          BLACK,
          DARK_BLUE,
          DARK_GREEN,
          DARK_AQUA,
          DARK_RED,
          DARK_PURPLE,
          GOLD,
          GRAY,
          DARK_GRAY,
          BLUE,
          GREEN,
          AQUA,
          RED,
          LIGHT_PURPLE,
          YELLOW,
          WHITE,
          MAGIC,
          BOLD,
          STRIKETHROUGH,
          UNDERLINE,
          ITALIC,
          RESET
        };
  }

  private final String name;
  private final Scoreboard scoreboard;
  private int id;

  ScoreboardCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers,
      @NotNull final Dimension dimension,
      @NotNull final DelayConfiguration delay,
      @NotNull final Identifier<Integer> id) {
    super(core, viewers, dimension, delay);
    checkArgument(id.getValue() >= 0, "Scoreboard id must be greater than or equal to 0!");
    this.name = "%s Video Player (%s)".formatted(core.getPlugin().getName(), id.getValue());
    this.scoreboard = this.setScoreboard();
  }

  private @NotNull Scoreboard setScoreboard() {
    return requireNonNull(this.getCore().getPlugin().getServer().getScoreboardManager())
        .getNewScoreboard();
  }

  @Override
  public void process(final int @NotNull [] data) {
    TaskUtils.sync(this.getCore(), this.processRunnable(IntBuffer.wrap(data)));
  }

  private @NotNull <T> Callable<T> processRunnable(@NotNull final IntBuffer data) {
    return () -> {
      final long time = System.currentTimeMillis();
      final Viewers viewers = this.getWatchers();
      final Dimension dimension = this.getDimensions();
      if (time - this.getLastUpdated() >= this.getDelayConfiguration().getDelay()) {
        this.setLastUpdated(time);
        this.setViewerScoreboards();
        this.displayScoreboard(viewers, dimension, data);
      }
      return null;
    };
  }

  private void setViewerScoreboards() {
    final Viewers viewers = this.getWatchers();
    for (final Player player : viewers.getPlayers()) {
      player.setScoreboard(this.scoreboard);
    }
  }

  private void displayScoreboard(
      @NotNull final Viewers viewers,
      @NotNull final Dimension dimension,
      @NotNull final IntBuffer data) {
    final UUID[] watchers = viewers.getViewers();
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();
    this.getPacketHandler()
        .displayScoreboard(watchers, this.scoreboard, data, this.name, width, height);
  }

  @Override
  @SuppressWarnings("deprecated")
  public void preparePlayerStateChange(@NotNull final PlayerControls status) {
    super.preparePlayerStateChange(status);
    if (status == PlayerControls.START) {
      this.registerScreen();
    }
  }

  private void registerScreen() {
    final Objective objective = this.getObjective();
    for (int i = 0; i < this.getDimensions().getHeight(); i++) {
      this.registerTeam(objective, i);
    }
  }

  private void registerTeam(@NotNull final Objective objective, final int i) {
    final Team team = this.scoreboard.registerNewTeam("SLOT_" + i);
    final String entry = COLORS[i].toString();
    team.addEntry(entry);
    objective.getScore(entry).setScore(15 - i);
  }

  private @NotNull Objective getObjective() {
    final Objective objective =
        this.scoreboard.registerNewObjective("rd-" + this.id++, "dummy", this.name);
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    return objective;
  }

  @Override
  public @NotNull String getScoreboardName() {
    return this.name;
  }

  @Override
  public int getScoreboardId() {
    return 0;
  }

  public static final class Builder extends CallbackBuilder {

    private Identifier<Integer> id;

    public Builder() {}

    @Contract("_ -> this")
    @Override
    public @NotNull Builder delay(@NotNull final DelayConfiguration delay) {
      super.delay(delay);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public @NotNull Builder dims(@NotNull final Dimension dims) {
      super.dims(dims);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public @NotNull Builder viewers(@NotNull final Viewers viewers) {
      super.viewers(viewers);
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder id(@NotNull final Identifier<Integer> id) {
      this.id = id;
      return this;
    }

    @Override
    public @NotNull FrameCallback build(@NotNull final MediaLibraryCore core) {
      return new ScoreboardCallback(
          core, this.getViewers(), this.getDims(), this.getDelay(), this.id);
    }
  }
}
