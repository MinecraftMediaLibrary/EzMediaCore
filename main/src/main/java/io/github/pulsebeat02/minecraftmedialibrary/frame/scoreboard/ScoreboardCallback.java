/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.frame.scoreboard;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/**
 * The callback used to update scoreboard for each frame when necessary.
 *
 * <p>By default, the scoreboard name will be set to the plugin name plus a space and "Video
 * Player". For example, if your plugin name was called "MyPlugin", the scoreboard which would be
 * used would be named: "MyPlugin Video Player".
 */
@SuppressWarnings("unchecked")
public final class ScoreboardCallback implements ScoreboardCallbackPrototype {

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

  private final MediaLibrary library;
  private final Set<Player> viewers;
  private final int videoWidth;
  private final int delay;
  private final int width;
  private final int height;
  private final String name;
  private long lastUpdated;

  private Scoreboard scoreboard;
  private int id;

  /**
   * Instantiates a new ScoreboardCallback.
   *
   * @param library the library
   * @param viewers the viewers
   * @param width the width
   * @param height the height
   * @param videoWidth the video width
   * @param delay the delay
   */
  public ScoreboardCallback(
      @NotNull final MediaLibrary library,
      final UUID[] viewers,
      final int width,
      final int height,
      final int videoWidth,
      final int delay) {
    this.library = library;
    this.viewers = Collections.newSetFromMap(new WeakHashMap<>());
    this.viewers.addAll(Arrays.stream(viewers).map(Bukkit::getPlayer).collect(Collectors.toSet()));
    this.width = width;
    this.height = height;
    name = library.getPlugin().getName() + " Video Player";
    this.videoWidth = videoWidth;
    this.delay = delay;
  }

  /**
   * Returns a new builder class to use.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Sends the necessary data onto the clouds while dithering.
   *
   * @param data to send
   */
  @Override
  public void send(final int[] data) {
    final long time = System.currentTimeMillis();
    if (time - lastUpdated >= delay) {
      lastUpdated = time;
      if (scoreboard == null) {
        scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        final Objective objective = scoreboard.registerNewObjective("rd-" + id++, "dummy", name);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 0; i < height; i++) {
          final Team team = scoreboard.registerNewTeam("SLOT_" + i);
          final String entry = COLORS[i].toString();
          team.addEntry(entry);
          objective.getScore(entry).setScore(15 - i);
        }
      }
      for (final Player player : viewers) {
        player.setScoreboard(scoreboard);
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
        for (final Player player : viewers) {
          player.sendMessage(msg.toString());
        }
        final Team team = scoreboard.getTeam("SLOT_" + y);
        if (team != null) {
          team.setSuffix(msg.toString());
        }
      }
    }
  }

  @Override
  public Set<Player> getViewers() {
    return viewers;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public int getDelay() {
    return delay;
  }

  @Override
  public MediaLibrary getLibrary() {
    return library;
  }

  @Override
  public int getVideoWidth() {
    return videoWidth;
  }

  @Override
  public long getLastUpdated() {
    return lastUpdated;
  }

  /** The type Builder. */
  public static class Builder {

    private UUID[] viewers;
    private int width;
    private int height;
    private int delay;

    private Builder() {}

    /**
     * Sets viewers.
     *
     * @param viewers the viewers
     * @return the viewers
     */
    public Builder setViewers(@NotNull final UUID[] viewers) {
      this.viewers = viewers;
      return this;
    }

    /**
     * Sets width.
     *
     * @param width the width
     * @return the width
     */
    public Builder setScoreboardWidth(final int width) {
      this.width = width;
      return this;
    }

    /**
     * Sets height.
     *
     * @param height the height
     * @return the height
     */
    public Builder setScoreboardHeight(final int height) {
      this.height = height;
      return this;
    }

    /**
     * Sets delay.
     *
     * @param delay the delay
     * @return the delay
     */
    public Builder setDelay(final int delay) {
      this.delay = delay;
      return this;
    }

    /**
     * Create entity cloud callback
     *
     * @param library the library
     * @return the entity cloud callback
     */
    public ScoreboardCallback build(final MediaLibrary library) {
      return new ScoreboardCallback(library, viewers, width, height, width, delay);
    }
  }
}
