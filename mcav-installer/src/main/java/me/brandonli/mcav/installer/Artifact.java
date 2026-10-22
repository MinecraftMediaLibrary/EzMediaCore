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
package me.brandonli.mcav.installer;

/**
 * The modules of the library that can be downloaded at runtime. Each module brings its own transitive
 * dependencies; {@link #COMMON} is required by every other module.
 *
 * <p>This enum is thread-safe.
 *
 * @since 1.0.0
 */
public enum Artifact {
  /**
   * The core library: players, filters, dithering, and installers.
   *
   * @since 1.0.0
   */
  COMMON("mcav-common"),
  /**
   * Discord voice output through JDA.
   *
   * @since 1.0.0
   */
  DISCORD("mcav-discord"),
  /**
   * The web audio player served over HTTP.
   *
   * @since 1.0.0
   */
  HTTP("mcav-http"),
  /**
   * Web page streaming through an embedded Chromium (JCEF).
   *
   * @since 1.0.0
   */
  BROWSER("mcav-browser"),
  /**
   * Virtual machines through QEMU.
   *
   * @since 1.0.0
   */
  VM("mcav-vm"),
  /**
   * Remote desktops through VNC.
   *
   * @since 1.0.0
   */
  VNC("mcav-vnc"),
  /**
   * Positional audio through Simple Voice Chat.
   *
   * @since 1.0.0
   */
  VOICECHAT("mcav-voicechat"),
  /**
   * OpenGL texture output through LWJGL.
   *
   * @since 1.0.0
   */
  LWJGL("mcav-lwjgl"),
  /**
   * Minecraft Paper integration: maps, blocks, entities, chat, scoreboards, and resource packs.
   *
   * @since 1.0.0
   */
  BUKKIT("mcav-bukkit");

  /**
   * The Maven group of every module.
   *
   * @since 1.0.0
   */
  public static final String GROUP_ID = "me.brandonli";

  /**
   * The version downloaded when none is specified.
   *
   * @since 1.0.0
   */
  public static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT";

  private final String artifactId;

  Artifact(final String artifactId) {
    this.artifactId = artifactId;
  }

  /**
   * Returns the Maven artifact id of the module.
   *
   * @return the artifact id, such as {@code mcav-common}
   * @since 1.0.0
   */
  public String getArtifactId() {
    return this.artifactId;
  }
}
