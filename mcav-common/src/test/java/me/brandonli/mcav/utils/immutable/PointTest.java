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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;
import me.brandonli.mcav.testing.EqualityAssertions;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Point}.
 */
final class PointTest {

  @Test
  void shouldStoreCoordinates() {
    final Point point = Point.point(1.5, -2.25);
    final double xCoordinate = point.getX();
    final double yCoordinate = point.getY();
    assertEquals(1.5, xCoordinate);
    assertEquals(-2.25, yCoordinate);
  }

  @Test
  void shouldFollowTheEqualityContract() {
    final Point point = Point.point(1.0, 2.0);
    final Point equalPoint = Point.point(1.0, 2.0);
    final Point otherX = Point.point(3.0, 2.0);
    final Point otherY = Point.point(1.0, 3.0);
    EqualityAssertions.assertEqualityContract(point, equalPoint, otherX, otherY);
  }

  @Test
  void shouldHashTheBitsOfBothCoordinates() {
    final Point point = Point.point(1.5, 3.0);

    final int hash = point.hashCode();
    final int expected = Objects.hash(1.5, 3.0);
    assertEquals(expected, hash);
  }

  @Test
  void shouldTreatNotANumberAsEqualToItself() {
    final Point point = Point.point(Double.NaN, 0.0);
    final Point equalPoint = Point.point(Double.NaN, 0.0);
    EqualityAssertions.assertEqualityContract(point, equalPoint);
  }

  @Test
  void shouldRenderBothCoordinates() {
    final Point point = Point.point(0.5, 4.0);
    final String text = point.toString();
    assertEquals("Point{xCoordinate=0.5, yCoordinate=4.0}", text);
  }
}
