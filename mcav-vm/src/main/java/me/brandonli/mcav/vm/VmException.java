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
package me.brandonli.mcav.vm;

import java.io.Serial;
import me.brandonli.mcav.MCAVException;

/**
 * Thrown when a virtual machine fails for a reason outside the caller's control, such as a QEMU program that is not
 * installed, a VNC port that is taken, a password file that cannot be written, or a QEMU process that cannot start,
 * exits early or never opens its display.
 *
 * <p>Every failure of {@code mcav-vm} that comes from QEMU or the machine is this type or a subtype; catch it to
 * handle them all. A failed VNC connection to the display of QEMU is a {@link me.brandonli.mcav.vnc.VncException}.
 */
public class VmException extends MCAVException {

  @Serial
  private static final long serialVersionUID = 3223541014952021143L;

  /**
   * Creates an exception with a message.
   *
   * @param message the detail message
   */
  public VmException(final String message) {
    super(message);
  }

  /**
   * Creates an exception with a message and the exception that caused it.
   *
   * @param message the detail message
   * @param cause   the cause
   */
  public VmException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
