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
package me.brandonli.mcav.bukkit.media.map;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The part of a single map that is covered by the image of a {@link MapLayout}.
 *
 * <p>The region has two coordinate systems. The local coordinates describe where the covered rectangle lies
 * inside the 128x128 map, and the source coordinates describe which pixel of the image is drawn at the local
 * origin. When the image is smaller than the map grid, maps at the border are only partly covered, and maps that
 * the image does not reach at all have an empty region.
 *
 * <p>Instances are immutable and thread-safe, but {@link #of(int, int, int, int, int, int)} stores values without
 * validation. For a valid nonempty map region, local coordinates must be nonnegative, dimensions positive, and both
 * local extents at most 128 pixels; source coordinates identify valid pixels in the source image. Obtain validated
 * regions through {@link MapLayout#getRegion(int)} when describing an image layout.
 *
 * @since 1.0.0
 */
public final class MapRegion {

  private final int localX;
  private final int localY;
  private final int width;
  private final int height;
  private final int sourceX;
  private final int sourceY;

  private MapRegion(final int localX, final int localY, final int width, final int height, final int sourceX, final int sourceY) {
    this.localX = localX;
    this.localY = localY;
    this.width = width;
    this.height = height;
    this.sourceX = sourceX;
    this.sourceY = sourceY;
  }

  /**
   * Returns a region of the given parts.
   *
   * @param localX  the x coordinate of the covered rectangle inside the map
   * @param localY  the y coordinate of the covered rectangle inside the map
   * @param width   the width of the covered rectangle, or zero if the map is not covered
   * @param height  the height of the covered rectangle, or zero if the map is not covered
   * @param sourceX the x coordinate of the image pixel that is drawn at {@code localX}
   * @param sourceY the y coordinate of the image pixel that is drawn at {@code localY}
   * @return the region
   * @since 1.0.0
   */
  public static MapRegion of(final int localX, final int localY, final int width, final int height, final int sourceX, final int sourceY) {
    return new MapRegion(localX, localY, width, height, sourceX, sourceY);
  }

  /**
   * Returns the x coordinate of the covered rectangle inside the map.
   *
   * @return the local x coordinate
   * @since 1.0.0
   */
  public int getLocalX() {
    return this.localX;
  }

  /**
   * Returns the y coordinate of the covered rectangle inside the map.
   *
   * @return the local y coordinate
   * @since 1.0.0
   */
  public int getLocalY() {
    return this.localY;
  }

  /**
   * Returns the width of the covered rectangle.
   *
   * @return the width in pixels, or zero if the map is not covered
   * @since 1.0.0
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Returns the height of the covered rectangle.
   *
   * @return the height in pixels, or zero if the map is not covered
   * @since 1.0.0
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Returns the x coordinate of the image pixel that is drawn at the local x coordinate.
   *
   * @return the source x coordinate
   * @since 1.0.0
   */
  public int getSourceX() {
    return this.sourceX;
  }

  /**
   * Returns the y coordinate of the image pixel that is drawn at the local y coordinate.
   *
   * @return the source y coordinate
   * @since 1.0.0
   */
  public int getSourceY() {
    return this.sourceY;
  }

  /**
   * Returns whether the image does not cover the map at all.
   *
   * @return true if either stored dimension is zero or negative
   * @since 1.0.0
   */
  public boolean isEmpty() {
    return this.width <= 0 || this.height <= 0;
  }

  /**
   * Returns whether {@code other} is a region with the same coordinates and size.
   *
   * @param other the object to compare with
   * @return whether every part matches
   * @since 1.0.0
   */
  @Override
  public boolean equals(final @Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof final MapRegion region)) {
      return false;
    }

    return (
      this.localX == region.localX &&
      this.localY == region.localY &&
      this.width == region.width &&
      this.height == region.height &&
      this.sourceX == region.sourceX &&
      this.sourceY == region.sourceY
    );
  }

  /**
   * Returns a hash code built from every part.
   *
   * @return the hash code
   * @since 1.0.0
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.localX, this.localY, this.width, this.height, this.sourceX, this.sourceY);
  }

  /**
   * Returns a debugging representation with every part.
   *
   * @return the representation
   * @since 1.0.0
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("localX", this.localX)
      .add("localY", this.localY)
      .add("width", this.width)
      .add("height", this.height)
      .add("sourceX", this.sourceX)
      .add("sourceY", this.sourceY)
      .toString();
  }
}
