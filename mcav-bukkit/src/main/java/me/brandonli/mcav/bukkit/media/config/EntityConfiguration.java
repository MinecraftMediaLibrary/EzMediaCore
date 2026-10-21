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
import org.bukkit.Location;

/**
 * Describes an image drawn as colored text inside a text display entity, for use with the entity image and entity
 * result.
 *
 * <p>Every pixel is drawn as the configured character in the color of the pixel, and every row of pixels becomes
 * one line of text. The entity always faces the viewer horizontally. Visibility is checked every server tick,
 * including viewers added after spawning and players who reconnect; removed online viewers are hidden.
 *
 * <p>The viewers collection is not copied, so a concurrent collection can be passed.
 *
 * <p>The position is retained by reference. Finish setting it before creating the display and do not
 * mutate it while rendering. {@link #getPosition()} returns that same mutable location.
 */
public final class EntityConfiguration {

  private final Collection<UUID> viewers;
  private final String character;
  private final int entityWidth;
  private final int entityHeight;
  private final Location position;

  EntityConfiguration(
    final Collection<UUID> viewers,
    final String character,
    final int entityWidth,
    final int entityHeight,
    final Location position
  ) {
    this.viewers = viewers;
    this.character = character;
    this.entityWidth = entityWidth;
    this.entityHeight = entityHeight;
    this.position = position;
  }

  /**
   * Creates a new builder for an entity configuration.
   *
   * @return a new builder
   */
  public static EntityConfigurationBuilder builder() {
    return new DefaultEntityConfigurationBuilder();
  }

  /**
   * Returns the players who see the entity.
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
   * Returns the width of the image in characters.
   *
   * @return the width in characters
   */
  public int getEntityWidth() {
    return this.entityWidth;
  }

  /**
   * Returns the height of the image in lines.
   *
   * @return the height in lines
   */
  public int getEntityHeight() {
    return this.entityHeight;
  }

  /**
   * Returns the position the entity is spawned at.
   *
   * @return the original location supplied to the builder, without a defensive copy
   */
  public Location getPosition() {
    return this.position;
  }
}
