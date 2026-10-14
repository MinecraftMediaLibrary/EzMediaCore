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

import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.ordered.OrderedDither;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.ordered.PixelMapper;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.palette.DitherPalette;

/**
 * Builds ordered dithering algorithms.
 */
public interface OrderedDitherBuilder extends DitherAlgorithmBuilder {
  /**
   * Creates the algorithm.
   *
   * @return the algorithm
   */
  @Override
  OrderedDither build();

  /**
   * Sets the palette the algorithm reduces images to. Defaults to the Minecraft map palette.
   *
   * @param palette the palette
   * @return this builder
   * @throws NullPointerException if {@code palette} is null
   */
  @Override
  OrderedDitherBuilder withPalette(final DitherPalette palette);

  /**
   * Sets the threshold pattern. Defaults to a 2x2 Bayer matrix at normal strength.
   *
   * @param mapper the threshold pattern
   * @return this builder
   * @throws NullPointerException if {@code mapper} is null
   */
  OrderedDitherBuilder withDitherMatrix(final PixelMapper mapper);
}
