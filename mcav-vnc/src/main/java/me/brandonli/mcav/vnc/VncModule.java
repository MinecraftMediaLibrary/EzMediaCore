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
package me.brandonli.mcav.vnc;

import me.brandonli.mcav.module.MCAVModule;

/**
 * Registers the VNC backend. The VNC client is pure Java and needs nothing else, so the module only has to be
 * installed:
 *
 * {@snippet :
 * final MCAVApi api = MCAV.api();
 * api.install(VncModule.class);
 * }
 *
 * <p>The module holds no state; stopping it does not release players, so release each {@link VncPlayer} you
 * created.
 *
 * <p>This class is thread-safe.
 *
 * @since 1.0.0
 */
public final class VncModule implements MCAVModule {

  private static final String MODULE_NAME = "vnc";

  /**
   * Creates the module. The module loader calls this for you.
   *
   * @since 1.0.0
   */
  public VncModule() {}

  /**
   * Starts the module; no resources are allocated. Players connect independently.
   *
   * @since 1.0.0
   */
  @Override
  public void start() {}

  /**
   * Stops the module; no players are released. Call {@link VncPlayer#release()} on each owned player.
   *
   * @since 1.0.0
   */
  @Override
  public void stop() {}

  /**
   * Returns the module identifier.
   *
   * @return {@code vnc}
   * @since 1.0.0
   */
  @Override
  public String getModuleName() {
    return MODULE_NAME;
  }
}
