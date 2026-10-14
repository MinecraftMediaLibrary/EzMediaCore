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
package me.brandonli.mcav.media.source.file;

import com.google.common.base.Preconditions;
import java.nio.file.Path;
import me.brandonli.mcav.media.source.StaticSource;

/**
 * A media file on the local file system.
 *
 * <p>Implementations in this library are immutable and thread-safe.
 */
public interface FileSource extends StaticSource {
  /**
   * Creates a source for a file. The file does not have to exist yet.
   *
   * @param path the path of the file
   * @return the source
   * @throws NullPointerException if {@code path} is null
   */
  static FileSource path(final Path path) {
    Preconditions.checkNotNull(path, "Path must not be null!");

    return new DefaultFileSource(path);
  }

  /**
   * Returns the path of the file.
   *
   * @return the path
   */
  Path getPath();

  /**
   * Creates a handle for reading from and writing to the file.
   *
   * @return the writable handle
   */
  default Writable createWritable() {
    final Path path = this.getPath();
    return new DefaultWritable(path);
  }

  /**
   * Returns a short name of the source type, such as {@code file} or {@code uri}, for log messages.
   *
   * @return the type name
   */
  @Override
  default String getName() {
    return "file";
  }

  /**
   * Returns the resource description, such as a file path, a URL, or a device index. Generated and specialized
   * sources may return a diagnostic string that cannot be opened by an arbitrary backend.
   *
   * @return the resource string
   */
  @Override
  default String getResource() {
    final Path path = this.getPath();
    return path.toString();
  }
}
