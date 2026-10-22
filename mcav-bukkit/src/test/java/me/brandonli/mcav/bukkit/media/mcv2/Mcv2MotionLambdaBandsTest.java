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
package me.brandonli.mcav.bukkit.media.mcv2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import me.brandonli.mcav.bukkit.media.mcv2.Mcv2Internals.MotionLambda;
import me.brandonli.mcav.bukkit.media.mcv2.Mcv2Internals.Workers;
import org.junit.jupiter.api.Test;

final class Mcv2MotionLambdaBandsTest {

  private static final int SAMPLES = 50_000;

  private static final int LUMA_STEP = 36;

  @Test
  void shouldCountEverySampleOfEveryBandOnce() {
    final int[] current = new int[SAMPLES];
    Arrays.fill(current, LUMA_STEP);
    final int[] previous = new int[SAMPLES];
    final double information = MotionLambda.temporalInformation(current, previous, Workers.SEQUENTIAL);
    assertEquals(1.0, information);

    // one sample differing by a thousand steps moves the mean by a thousand over the samples, wherever it lies
    final int[] spike = new int[SAMPLES];
    spike[SAMPLES - 1] = 1_000 * LUMA_STEP;
    final double expectedSpikeInformation = 1_000.0 / SAMPLES;
    final double spikeInformation = MotionLambda.temporalInformation(spike, previous, Workers.SEQUENTIAL);
    assertEquals(expectedSpikeInformation, spikeInformation, 1e-12);
  }
}
