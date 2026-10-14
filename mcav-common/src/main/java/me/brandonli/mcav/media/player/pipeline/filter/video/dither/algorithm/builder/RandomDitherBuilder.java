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
package me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.builder;

import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.random.RandomDither;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.palette.DitherPalette;

/**
 * Builds random dithering algorithms.
 *
 * <p>Builders are not thread-safe; use one per thread.
 */
public interface RandomDitherBuilder extends DitherAlgorithmBuilder {
  /**
   * Creates the algorithm.
   *
   * @return the algorithm
   */
  @Override
  RandomDither build();

  /**
   * Sets the palette the algorithm reduces images to. Defaults to the Minecraft map palette.
   *
   * @param palette the palette
   * @return this builder
   * @throws NullPointerException if {@code palette} is null
   */
  @Override
  RandomDitherBuilder withPalette(final DitherPalette palette);

  /**
   * Sets the largest noise value added to or subtracted from a channel. Defaults to
   * {@link RandomDither#NORMAL_WEIGHT}.
   *
   * @param weight the weight from 0 to 255
   * @return this builder
   * @throws IllegalArgumentException if the weight is outside 0 through 255
   */
  RandomDitherBuilder withWeight(final int weight);
}
