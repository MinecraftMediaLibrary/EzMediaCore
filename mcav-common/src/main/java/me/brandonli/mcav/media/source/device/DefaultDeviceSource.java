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
package me.brandonli.mcav.media.source.device;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The default {@link DeviceSource}.
 *
 * <p>This class is immutable and thread-safe.
 */
public final class DefaultDeviceSource implements DeviceSource {

  private final int deviceId;

  DefaultDeviceSource(final int deviceId) {
    this.deviceId = deviceId;
  }

  /**
   * Returns the index of the device.
   *
   * @return the device index
   */
  @Override
  public int getDeviceId() {
    return this.deviceId;
  }

  /**
   * Returns whether {@code other} is a device source with the same device index.
   *
   * @param other the object to compare with
   * @return whether both match
   */
  @Override
  public boolean equals(final @Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof final DefaultDeviceSource source)) {
      return false;
    }

    return this.deviceId == source.deviceId;
  }

  /**
   * Returns a hash code built from the device index.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.deviceId);
  }

  /**
   * Returns a debugging representation with the device index.
   *
   * @return the representation
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("deviceId", this.deviceId).toString();
  }
}
