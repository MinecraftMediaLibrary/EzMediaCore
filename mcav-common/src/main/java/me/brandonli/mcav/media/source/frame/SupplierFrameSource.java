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
package me.brandonli.mcav.media.source.frame;

/**
 * The default {@link FrameSource}.
 */
public final class SupplierFrameSource implements FrameSource {

  private final SampleSupplier supplier;
  private final int width;
  private final int height;
  private final float frameRate;

  SupplierFrameSource(final SampleSupplier supplier, final int width, final int height, final float frameRate) {
    this.supplier = supplier;
    this.width = width;
    this.height = height;
    this.frameRate = frameRate;
  }

  /**
   * Returns the supplier of frames.
   *
   * @return the supplier
   */
  @Override
  public SampleSupplier supplyFrameSamples() {
    return this.supplier;
  }

  /**
   * Returns the width of the frames.
   *
   * @return the width in pixels
   */
  @Override
  public int getFrameWidth() {
    return this.width;
  }

  /**
   * Returns the height of the frames.
   *
   * @return the height in pixels
   */
  @Override
  public int getFrameHeight() {
    return this.height;
  }

  /**
   * Returns the rate at which frames are requested from the supplier.
   *
   * @return the frame rate in frames per second
   */
  @Override
  public float getFrameRate() {
    return this.frameRate;
  }
}
