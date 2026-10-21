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
package me.brandonli.mcav.bukkit.resourcepack.provider.http;

import java.io.Serial;
import me.brandonli.mcav.bukkit.BukkitException;

/**
 * Thrown when the dedicated resource pack HTTP server cannot be started, usually because its port is in use.
 *
 * <p>A plugin can catch it, report the port, and disable itself or choose another port.
 */
public final class HttpServerException extends BukkitException {

  @Serial
  private static final long serialVersionUID = -1558230210828592665L;

  /**
   * Constructs a new exception with a detail message and no cause.
   *
   * @param message the detail message, which names the port that cannot be bound
   */
  HttpServerException(final String message) {
    super(message);
  }

  /**
   * Constructs a new exception with a detail message and the failure that caused it.
   *
   * @param message the detail message, which names the port that cannot be bound
   * @param cause   the failure that caused this exception, such as the bind failure
   */
  HttpServerException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
