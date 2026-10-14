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
package me.brandonli.mcav.utils.immutable;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An immutable point in two dimensions.
 */
public final class Point {

  private final double xCoordinate;
  private final double yCoordinate;

  Point(final double xCoordinate, final double yCoordinate) {
    this.xCoordinate = xCoordinate;
    this.yCoordinate = yCoordinate;
  }

  /**
   * Creates a point without validating its coordinates. Units depend on the consuming API; NaN, infinities
   * and signed zero are retained. Equality uses {@link Double#compare(double, double)} for each coordinate.
   *
   * @param xCoordinate the x coordinate
   * @param yCoordinate the y coordinate
   * @return the point
   */
  public static Point point(final double xCoordinate, final double yCoordinate) {
    return new Point(xCoordinate, yCoordinate);
  }

  /**
   * Returns the x coordinate.
   *
   * @return the x coordinate
   */
  public double getX() {
    return this.xCoordinate;
  }

  /**
   * Returns the y coordinate.
   *
   * @return the y coordinate
   */
  public double getY() {
    return this.yCoordinate;
  }

  @Override
  public boolean equals(final @Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof final Point point)) {
      return false;
    }

    final int xComparison = Double.compare(this.xCoordinate, point.xCoordinate);
    final int yComparison = Double.compare(this.yCoordinate, point.yCoordinate);
    return xComparison == 0 && yComparison == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.xCoordinate, this.yCoordinate);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("xCoordinate", this.xCoordinate).add("yCoordinate", this.yCoordinate).toString();
  }
}
