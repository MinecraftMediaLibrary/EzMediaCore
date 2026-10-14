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
package me.brandonli.mcav.capability;

/**
 * An optional feature of the library that depends on an external program or on native libraries that not every
 * system can load. Check availability with {@link me.brandonli.mcav.MCAVApi#hasCapability(Capability)} before using
 * the feature, or wait for it with {@link me.brandonli.mcav.MCAVApi#whenCapabilityReady(Capability)}.
 *
 * <p>{@link #FFMPEG} and {@link #FACE_DETECTION} are decided while {@link me.brandonli.mcav.MCAVApi#install(Class[])}
 * runs. {@link #VLC} and {@link #YT_DLP} are external programs that may have to be downloaded first, so they are
 * prepared in the background after {@code install} returned.
 *
 * <p>This enum is immutable and thread-safe.
 *
 * @since 1.0.0
 */
public enum Capability {
  /**
   * Playback through the VLC media player, which supports the widest range of formats and streams. Prepared in the
   * background, because VLC may have to be downloaded first.
   *
   * @since 1.0.0
   */
  VLC("VLC"),

  /**
   * Playback through the bundled FFmpeg libraries. Successful core installation requires these natives to load.
   *
   * @since 1.0.0
   */
  FFMPEG("FFmpeg"),

  /**
   * Resolving stream URLs of websites such as YouTube through yt-dlp. Prepared in the background, because yt-dlp may
   * have to be downloaded first.
   *
   * @since 1.0.0
   */
  YT_DLP("yt-dlp"),

  /**
   * Face detection with the object detection module of OpenCV. Its native libraries link OpenCV's GUI module, which
   * needs GTK 2 on Linux. On 64-bit x86 and ARM Linux systems without GTK 2, MCAV installs it from pinned Debian
   * packages into its cache; where that fails, the feature is missing.
   *
   * @since 1.0.0
   */
  FACE_DETECTION("Face detection");

  private final String displayName;

  Capability(final String displayName) {
    this.displayName = displayName;
  }

  /**
   * Returns the name of the capability as it is written in log and error messages, such as {@code yt-dlp}.
   *
   * @return the display name
   * @since 1.0.0
   */
  public String getDisplayName() {
    return this.displayName;
  }
}
