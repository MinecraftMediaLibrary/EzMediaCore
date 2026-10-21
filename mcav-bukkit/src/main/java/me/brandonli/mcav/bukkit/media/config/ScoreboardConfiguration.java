/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.bukkit.media.config;

import java.util.Collection;
import java.util.UUID;
import me.brandonli.mcav.bukkit.media.result.Characters;

/**
 * Describes an image drawn onto the sidebar scoreboard, for use with the scoreboard image and scoreboard result.
 *
 * <p>Every pixel is drawn as the configured character in the color of the pixel, and every row of pixels becomes
 * one line of the sidebar. The sidebar shows at most 15 lines.
 *
 * <p>The viewers collection is not copied, so a concurrent collection can be passed. The renderer checks membership each server tick,
 * shows the board to new or returning online viewers, and restores removed viewers who still see its board.
 */
public final class ScoreboardConfiguration {

  /**
   * The maximum number of lines the sidebar can show.
   */
  public static final int MAXIMUM_LINES = 15;

  private final Collection<UUID> viewers;
  private final String character;
  private final int lines;
  private final int width;

  ScoreboardConfiguration(final Collection<UUID> viewers, final String character, final int lines, final int width) {
    this.viewers = viewers;
    this.character = character;
    this.lines = lines;
    this.width = width;
  }

  /**
   * Creates a new builder for a scoreboard configuration.
   *
   * @return a new builder
   */
  public static ScoreboardConfigurationBuilder builder() {
    return new DefaultScoreboardConfigurationBuilder();
  }

  /**
   * Returns the players who see the scoreboard.
   *
   * @return the original mutable-or-immutable collection supplied to the builder, without a copy; use a
   *         concurrent collection if another thread changes membership during rendering
   */
  public Collection<UUID> getViewers() {
    return this.viewers;
  }

  /**
   * Returns the text drawn for every pixel, usually a single character such as {@code █}.
   *
   * @return the pixel text
   * @see Characters
   */
  public String getCharacter() {
    return this.character;
  }

  /**
   * Returns the height of the image in lines.
   *
   * @return the number of lines, at most {@link #MAXIMUM_LINES}
   */
  public int getLines() {
    return this.lines;
  }

  /**
   * Returns the width of the image in characters.
   *
   * @return the width in characters
   */
  public int getWidth() {
    return this.width;
  }
}
