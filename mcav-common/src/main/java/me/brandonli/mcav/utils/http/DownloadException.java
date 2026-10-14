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
package me.brandonli.mcav.utils.http;

import java.io.Serial;
import me.brandonli.mcav.MCAVException;

/**
 * Thrown when a download or a request over HTTP fails, for example because the network fails, the server answers with
 * an error status, or the downloaded file does not match its pinned hash or size.
 *
 * <p>Subtypes name the failures a caller may want to tell apart, such as {@link HttpStatusException} and
 * {@link ChecksumMismatchException}.
 *
 * @since 1.0.0
 */
public class DownloadException extends MCAVException {

  @Serial
  private static final long serialVersionUID = -7488382407340889322L;

  DownloadException(final String message) {
    super(message);
  }

  DownloadException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
