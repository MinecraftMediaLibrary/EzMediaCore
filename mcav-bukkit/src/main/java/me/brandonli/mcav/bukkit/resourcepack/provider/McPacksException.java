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
package me.brandonli.mcav.bukkit.resourcepack.provider;

import java.io.Serial;
import me.brandonli.mcav.bukkit.BukkitException;

/**
 * Thrown when a resource pack cannot be uploaded to mc-packs.net.
 *
 * <p>The upload depends on the network and on a remote service, so trying again later may succeed; a plugin can also
 * catch it and fall back to another hosting.
 */
public final class McPacksException extends BukkitException {

  @Serial
  private static final long serialVersionUID = -6775463807604247034L;

  McPacksException(final String message) {
    super(message);
  }

  McPacksException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
