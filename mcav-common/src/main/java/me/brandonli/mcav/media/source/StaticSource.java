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
package me.brandonli.mcav.media.source;

/**
 * A source with a fixed length, such as a file, which can be seeked and played to its end.
 *
 * <p>Implementations in this library are immutable and thread-safe.
 *
 * @since 1.0.0
 */
public interface StaticSource extends Source {
  /**
   * Checks whether the source is classified as static, as opposed to a URI, device or generated frames.
   * This does not probe its actual duration or whether the selected decoder supports seeking.
   *
   * @return true for files and similar finite media
   * @since 1.0.0
   */
  @Override
  default boolean isStatic() {
    return true;
  }
}
