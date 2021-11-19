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
package io.github.pulsebeat02.ezmediacore.callback.implementation;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.CallbackBuilder;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.FrameCallback;
import io.github.pulsebeat02.ezmediacore.callback.Identifier;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.utility.task.TaskUtils;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Callable;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ScoreboardCallback extends FrameCallback implements ScoreboardCallbackDispatcher {

  private static ChatColor[] COLORS;

  static {
    try {
      final Field field = ChatColor.class.getDeclaredField("BY_CHAR");
      field.setAccessible(true);
      //noinspection unchecked
      COLORS = ((Map<Character, ChatColor>) field.get(null)).values().toArray(new ChatColor[0]);
      field.setAccessible(false);
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
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
    Preconditions.checkArgument(
        id.getValue() >= 0, "Scoreboard id must be greater than or equal to 0!");
    this.name = "%s Video Player (%s)".formatted(core.getPlugin().getName(), id.getValue());
    this.scoreboard = this.setScoreboard();
  }

  private @NotNull Scoreboard setScoreboard() {
    return Bukkit.getScoreboardManager().getNewScoreboard();
  }

  @Override
  public void process(final int[] data) {
    TaskUtils.sync(this.getCore(), this.processRunnable(data));
  }

  private @NotNull <T> Callable<T> processRunnable(final int @NotNull [] data) {
    return () -> {
      final long time = System.currentTimeMillis();
      if (time - this.getLastUpdated() >= this.getDelayConfiguration().getDelay()) {
        this.setLastUpdated(time);
        final Dimension dimension = this.getDimensions();
        final Viewers viewers = this.getWatchers();
        for (final Player player : viewers.getPlayers()) {
          player.setScoreboard(this.scoreboard);
        }
        this.getPacketHandler()
            .displayScoreboard(
                viewers.getViewers(),
                this.scoreboard,
                this.name,
                data,
                dimension.getWidth(),
                dimension.getHeight());
      }
      return null;
    };
  }

  @Override
  @SuppressWarnings("deprecated")
  public void preparePlayerStateChange(@NotNull final PlayerControls status) {
    super.preparePlayerStateChange(status);
    if (status == PlayerControls.START) {
      final Objective objective =
          this.scoreboard.registerNewObjective("rd-" + this.id++, "dummy", this.name);
      objective.setDisplaySlot(DisplaySlot.SIDEBAR);
      for (int i = 0; i < this.getDimensions().getHeight(); i++) {
        final Team team = this.scoreboard.registerNewTeam("SLOT_" + i);
        final String entry = COLORS[i].toString();
        team.addEntry(entry);
        objective.getScore(entry).setScore(15 - i);
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

  public static final class Builder extends CallbackBuilder {

    private Identifier<Integer> id;

    public Builder() {
    }

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
