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
package me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import me.brandonli.mcav.media.image.ImageBuffer;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.builder.NearestDitherBuilder;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.builder.NearestDitherBuilderImpl;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.builder.RandomDitherBuilder;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.builder.RandomDitherBuilderImpl;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.nearest.NearestDither;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.nearest.NearestDitherImpl;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.random.RandomDither;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.random.RandomDitherImpl;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.random.RandomNumberProvider;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.algorithm.random.XoroshiroRandomProvider;
import me.brandonli.mcav.media.player.pipeline.filter.video.dither.palette.DitherPalette;
import org.junit.jupiter.api.Test;

/**
 * Tests the random and nearest color algorithms, their builders and {@link XoroshiroRandomProvider}.
 */
final class RandomAndNearestDitherTest {

  private static byte[] dither(final DitherAlgorithm algorithm, final int[] pixels, final int width) {
    final int height = pixels.length / width;
    try (final ImageBuffer image = ImageBuffer.buffer(pixels, width, height)) {
      return algorithm.ditherIntoBytes(image);
    }
  }

  @Test
  void shouldPickTheNearestColorWhenRandomDitheringWithoutWeight() {
    final int[] pixels = DitherTestImages.gradient(64, 4);
    final RandomDitherImpl random = new RandomDitherImpl(DitherTestImages.BLACK_WHITE, 0);
    final NearestDither nearest = new NearestDitherImpl(DitherTestImages.BLACK_WHITE);
    final byte[] randomIndices = dither(random, pixels, 64);
    final byte[] nearestIndices = dither(nearest, pixels, 64);
    final int weight = random.getWeight();
    assertArrayEquals(nearestIndices, randomIndices);
    assertEquals(0, weight);
  }

  @Test
  void shouldRandomDitherReproduciblyWithTheSameSeed() {
    final int[] pixels = DitherTestImages.gradient(64, 16);
    final RandomDitherImpl first = new RandomDitherImpl(
      DitherTestImages.BLACK_WHITE,
      RandomDither.HEAVY_WEIGHT,
      new XoroshiroRandomProvider(7L)
    );
    final RandomDitherImpl second = new RandomDitherImpl(
      DitherTestImages.BLACK_WHITE,
      RandomDither.HEAVY_WEIGHT,
      new XoroshiroRandomProvider(7L)
    );
    final byte[] firstIndices = dither(first, pixels, 64);
    final byte[] secondIndices = dither(second, pixels, 64);
    final double ratio = DitherTestImages.whiteRatio(firstIndices);
    assertArrayEquals(firstIndices, secondIndices);
    final boolean halfWhite = ratio > 0.4 && ratio < 0.6;
    assertTrue(halfWhite, "ratio " + ratio);
  }

  @Test
  void shouldWritePaletteColorsWhenRandomDitheringInPlace() {
    final int[] pixels = DitherTestImages.gradient(32, 4);
    final RandomDitherImpl random = new RandomDitherImpl(DitherTestImages.BLACK_WHITE, RandomDither.LIGHT_WEIGHT);
    random.dither(pixels, 32);
    for (final int pixel : pixels) {
      final boolean paletteColor = pixel == 0xFF000000 || pixel == 0xFFFFFFFF;
      assertTrue(paletteColor);
    }
  }

  @Test
  void shouldCreateTheConfiguredAlgorithmFromTheRandomBuilder() {
    final RandomDitherBuilder<RandomDither, RandomDitherBuilderImpl> builder = DitherAlgorithm.random();
    final RandomDitherBuilderImpl weighted = builder.withWeight(RandomDither.NORMAL_WEIGHT);
    final RandomDitherBuilderImpl configured = weighted.withPalette(DitherTestImages.BLACK_WHITE);
    final RandomDither built = configured.build();
    final RandomDitherBuilder<RandomDither, RandomDitherBuilderImpl> defaultBuilder = DitherAlgorithm.random();
    final RandomDither defaults = defaultBuilder.build();
    final DitherPalette palette = built.getPalette();
    final DitherPalette defaultPalette = defaults.getPalette();
    assertSame(builder, weighted, "the builder returns itself for chaining");
    assertSame(builder, configured);
    assertSame(DitherTestImages.BLACK_WHITE, palette);
    assertSame(DitherPalette.DEFAULT_MAP_PALETTE, defaultPalette);
  }

  @Test
  void shouldRejectInvalidSettingsInTheRandomBuilderAndAlgorithm() {
    final RandomDitherBuilder<RandomDither, RandomDitherBuilderImpl> builder = DitherAlgorithm.random();
    assertThrows(IllegalArgumentException.class, () -> builder.withWeight(-1));
    assertThrows(IllegalArgumentException.class, () -> builder.withWeight(256));
    assertThrows(NullPointerException.class, () -> builder.withPalette(null));
    assertThrows(IllegalArgumentException.class, () -> new RandomDitherImpl(DitherTestImages.BLACK_WHITE, -1));
    assertThrows(IllegalArgumentException.class, () -> new RandomDitherImpl(DitherTestImages.BLACK_WHITE, 256));
    assertThrows(NullPointerException.class, () -> new RandomDitherImpl(DitherTestImages.BLACK_WHITE, 1, null));
  }

  @Test
  void shouldReportAMissingImageBeforeReadingItsPixelsWhenRandomDithering() {
    final RandomDitherImpl random = new RandomDitherImpl(DitherTestImages.BLACK_WHITE, 0);
    final NullPointerException missingImage = assertThrows(NullPointerException.class, () -> random.ditherIntoBytes(null));
    final String message = missingImage.getMessage();
    assertEquals("Image must not be null!", message);
  }

  @Test
  void shouldAcceptTheBoundaryWeights() {
    final RandomDitherBuilder<RandomDither, RandomDitherBuilderImpl> builder = DitherAlgorithm.random();
    final RandomDitherBuilderImpl lightest = builder.withWeight(0);
    final RandomDitherBuilderImpl heaviest = builder.withWeight(255);
    final RandomDitherImpl lowest = new RandomDitherImpl(DitherTestImages.BLACK_WHITE, 0);
    final RandomDitherImpl highest = new RandomDitherImpl(DitherTestImages.BLACK_WHITE, 255);
    final int lowestWeight = lowest.getWeight();
    final int highestWeight = highest.getWeight();
    assertSame(builder, lightest, "a weight of 0 is allowed");
    assertSame(builder, heaviest, "and so is a weight of 255");
    assertEquals(0, lowestWeight);
    assertEquals(255, highestWeight, "the algorithm reports the weight it was given");
  }

  @Test
  void shouldCheckTheWeightInTheConstructorWithAProviderAsWell() {
    final RandomNumberProvider provider = new XoroshiroRandomProvider(1L);
    assertThrows(IllegalArgumentException.class, () -> new RandomDitherImpl(DitherTestImages.BLACK_WHITE, -1, provider));
    assertThrows(IllegalArgumentException.class, () -> new RandomDitherImpl(DitherTestImages.BLACK_WHITE, 256, provider));
  }

  @Test
  void shouldCheckTheBufferWhenDitheringInPlace() {
    final NearestDither nearest = new NearestDitherImpl(DitherTestImages.BLACK_WHITE);
    final RandomDitherImpl random = new RandomDitherImpl(DitherTestImages.BLACK_WHITE, 0);
    assertThrows(IllegalArgumentException.class, () -> nearest.dither(new int[3], 2));
    assertThrows(IllegalArgumentException.class, () -> random.dither(new int[3], 2));
    assertThrows(IllegalArgumentException.class, () -> nearest.dither(new int[4], 0));
    assertThrows(NullPointerException.class, () -> nearest.dither(null, 2));
    assertThrows(NullPointerException.class, () -> random.dither(null, 2));
  }

  @Test
  void shouldScaleTheDoublesToTheWidthOfTheRangeWithXoroshiro() {
    final XoroshiroRandomProvider provider = new XoroshiroRandomProvider(42L);
    final double scaled = provider.nextDouble(0.0, 4.0);
    final double unit = 8119767394961995L / (double) (1L << 53);
    final double expected = unit * 4.0;
    assertEquals(expected, scaled, 1.0e-12, "the unit value is multiplied by the width of the range, not divided by it");
  }

  @Test
  void shouldTranslateNonzeroBoundsWithoutChangingTheDistributionWithXoroshiro() {
    final XoroshiroRandomProvider provider = new XoroshiroRandomProvider(42L);
    final double actual = provider.nextDouble(-5.0, 7.0);
    final double unit = 8119767394961995L / (double) (1L << 53);
    final double expected = -5.0 + 12.0 * unit;
    assertEquals(expected, actual, 1.0e-12);
  }

  @Test
  void shouldPreserveTheQuantileWhenTheRangeOverflowsWithXoroshiro() {
    final XoroshiroRandomProvider provider = new XoroshiroRandomProvider(42L);
    final double minimum = -Double.MAX_VALUE;
    final double maximum = Double.MAX_VALUE / 2.0;
    final double actual = provider.nextDouble(minimum, maximum);
    // The independently recorded C-reference output identifies this quantile.
    // Decimal arithmetic evaluates the convex combination without a double overflow.
    final BigDecimal numerator = BigDecimal.valueOf(8119767394961995L);
    final BigDecimal denominator = BigDecimal.valueOf(1L << 53);
    final BigDecimal unit = numerator.divide(denominator);
    final BigDecimal complement = BigDecimal.ONE.subtract(unit);
    final BigDecimal lower = new BigDecimal(minimum);
    final BigDecimal upper = new BigDecimal(maximum);
    final BigDecimal lowerPart = lower.multiply(complement);
    final BigDecimal upperPart = upper.multiply(unit);
    final BigDecimal weighted = lowerPart.add(upperPart);
    final double expected = weighted.doubleValue();
    final double tolerance = Math.ulp(expected) * 4.0;
    assertEquals(expected, actual, tolerance);
  }

  @Test
  void shouldReadTheBooleanFromTheSignOfTheOutputWithXoroshiro() {
    final XoroshiroRandomProvider provider = new XoroshiroRandomProvider(42L);
    final boolean firstDraw = provider.nextBoolean();
    final boolean secondDraw = provider.nextBoolean();
    final boolean thirdDraw = provider.nextBoolean();
    assertTrue(firstDraw, "the first output of seed 42 is negative, so its highest bit is set");
    assertFalse(secondDraw, "the second is positive");
    assertTrue(thirdDraw);
  }

  @Test
  void shouldSplitAtMidGrayWhenNearestDithering() {
    final int[] pixels = DitherTestImages.gradient(128, 64);
    final NearestDither nearest = new NearestDitherImpl(DitherTestImages.BLACK_WHITE);
    final byte[] indices = dither(nearest, pixels, 128);
    final byte firstIndex = indices[0];
    assertEquals(0, firstIndex);
    final byte darkerIndex = indices[63];
    assertEquals(0, darkerIndex, "126 is darker than the middle between black and white");
    final byte brighterIndex = indices[64];
    assertEquals(1, brighterIndex, "128 is brighter than the middle");
    final byte lastIndex = indices[127];
    assertEquals(1, lastIndex);
  }

  @Test
  void shouldReadEveryChannelFromItsOwnPlaceInThePackedColorWhenNearestDithering() {
    final DitherPalette blackRedGreen = DitherPalette.colors(0x000000, 0xFF0000, 0x00FF00);
    final NearestDitherImpl nearest = new NearestDitherImpl(blackRedGreen);
    final int[] pixels = { 0xFFFF0000, 0xFF00FF00 };
    nearest.dither(pixels, 2);
    final int[] expected = { 0xFFFF0000, 0xFF00FF00 };
    assertArrayEquals(expected, pixels, "a red pixel stays red and a green one green, so both channels are read correctly");
  }

  @Test
  void shouldMatchSerialWhenNearestDitheringOnASingleThread() {
    final int[] pixels = DitherTestImages.gradient(8, 8);
    final NearestDitherImpl nearest = new NearestDitherImpl(DitherTestImages.BLACK_WHITE);
    try (final ForkJoinPool pool = new ForkJoinPool(1); final ImageBuffer image = ImageBuffer.buffer(pixels, 8, 8)) {
      final byte[] serial = nearest.ditherIntoBytes(image);
      final byte[] parallel = nearest.ditherIntoBytes(image, pool);
      assertArrayEquals(serial, parallel);
    }
  }

  @Test
  void shouldMatchSerialWhenNearestDitheringALargeImageOnAWidePool() {
    final int[] pixels = DitherTestImages.gradient(1024, 1024);
    final NearestDitherImpl nearest = new NearestDitherImpl(DitherTestImages.BLACK_WHITE);
    try (final ForkJoinPool pool = new ForkJoinPool(32); final ImageBuffer image = ImageBuffer.buffer(pixels, 1024, 1024)) {
      final byte[] serial = nearest.ditherIntoBytes(image);
      final byte[] parallel = nearest.ditherIntoBytes(image, pool);
      assertArrayEquals(serial, parallel);
    }
  }

  @Test
  void shouldMatchSerialWhenNearestDitheringInParallel() {
    final int[] large = DitherTestImages.gradient(128, 64);
    final int[] small = DitherTestImages.gradient(8, 8);
    final NearestDitherImpl nearest = new NearestDitherImpl(DitherTestImages.BLACK_WHITE);
    final ForkJoinPool pool = new ForkJoinPool(2);
    try (
      final ImageBuffer largeImage = ImageBuffer.buffer(large, 128, 64);
      final ImageBuffer smallImage = ImageBuffer.buffer(small, 8, 8)
    ) {
      final byte[] largeSerial = nearest.ditherIntoBytes(largeImage);
      final byte[] largeParallel = nearest.ditherIntoBytes(largeImage, pool);
      final byte[] smallSerial = nearest.ditherIntoBytes(smallImage);
      final byte[] smallParallel = nearest.ditherIntoBytes(smallImage, pool);
      assertArrayEquals(largeSerial, largeParallel);
      assertArrayEquals(smallSerial, smallParallel);
      assertThrows(NullPointerException.class, () -> nearest.ditherIntoBytes(largeImage, null));
    } finally {
      pool.shutdownNow();
    }
    final NullPointerException missingImage = assertThrows(NullPointerException.class, () -> nearest.ditherIntoBytes(null));
    final NullPointerException missingParallelImage = assertThrows(NullPointerException.class, () -> nearest.ditherIntoBytes(null, pool));
    final String message = missingImage.getMessage();
    assertEquals("Image must not be null!", message);
    final String parallelMessage = missingParallelImage.getMessage();
    assertEquals("Image must not be null!", parallelMessage);
  }

  @Test
  void shouldNearestDitherInPlaceAndThroughBuilders() {
    final int[] pixels = { 0xFF101010, 0xFFF0F0F0 };
    final NearestDitherBuilder<NearestDither, NearestDitherBuilderImpl> builder = DitherAlgorithm.nearest();
    final NearestDitherBuilderImpl configured = builder.withPalette(DitherTestImages.BLACK_WHITE);
    final NearestDither nearest = configured.build();
    nearest.dither(pixels, 2);
    final NearestDither defaults = DitherAlgorithm.nearestColor();
    final DitherPalette defaultPalette = defaults.getPalette();
    assertSame(builder, configured, "the builder returns itself for chaining");
    assertArrayEquals(new int[] { 0xFF000000, 0xFFFFFFFF }, pixels);
    assertSame(DitherPalette.DEFAULT_MAP_PALETTE, defaultPalette);
    assertThrows(NullPointerException.class, () -> builder.withPalette(null));
  }

  @Test
  void shouldBeReproducibleWithTheSameSeedWithXoroshiro() {
    final XoroshiroRandomProvider first = new XoroshiroRandomProvider(42L);
    final XoroshiroRandomProvider second = new XoroshiroRandomProvider(42L);
    final XoroshiroRandomProvider other = new XoroshiroRandomProvider(43L);
    final int[] firstSequence = new int[1_000];
    final int[] otherSequence = new int[1_000];
    for (int draw = 0; draw < 1_000; draw++) {
      final int value = first.nextInt(-2, 3);
      final int sameValue = second.nextInt(-2, 3);
      firstSequence[draw] = value;
      otherSequence[draw] = other.nextInt(-2, 3);
      final double fraction = first.nextDouble(1.0, 2.0);
      final double sameFraction = second.nextDouble(1.0, 2.0);
      other.nextDouble(1.0, 2.0);
      final boolean drawn = first.nextBoolean();
      final boolean sameFlag = second.nextBoolean();
      other.nextBoolean();
      assertEquals(value, sameValue);
      assertEquals(fraction, sameFraction);
      assertEquals(drawn, sameFlag);
    }

    final boolean sameSequence = Arrays.equals(firstSequence, otherSequence);
    assertFalse(sameSequence, "different seeds differ at the same positions");
    final int firstNext = first.nextInt(0, Integer.MAX_VALUE);
    final int otherNext = other.nextInt(0, Integer.MAX_VALUE);
    assertNotEquals(firstNext, otherNext, "another seed produces another sequence");
  }

  @Test
  void shouldStayInRangeAndReachBothEndsWithXoroshiro() {
    final XoroshiroRandomProvider provider = new XoroshiroRandomProvider(42L);
    boolean sawMinimum = false;
    boolean sawMaximum = false;
    boolean sawTrue = false;
    boolean sawFalse = false;
    for (int draw = 0; draw < 1_000; draw++) {
      final int value = provider.nextInt(-2, 3);
      final double fraction = provider.nextDouble(1.0, 2.0);
      final boolean drawn = provider.nextBoolean();
      final boolean valueInRange = value >= -2 && value < 3;
      assertTrue(valueInRange);
      final boolean fractionInRange = fraction >= 1.0 && fraction < 2.0;
      assertTrue(fractionInRange);
      sawMinimum |= value == -2;
      sawMaximum |= value == 2;
      sawTrue |= drawn;
      sawFalse |= !drawn;
    }
    final boolean reachedEveryOutcome = sawMinimum && sawMaximum && sawTrue && sawFalse;
    assertTrue(reachedEveryOutcome);
  }

  @Test
  void shouldNeverRoundToTheExclusiveDoubleBoundWithXoroshiro() {
    final XoroshiroRandomProvider provider = new XoroshiroRandomProvider(3L);
    final double upper = Math.nextUp(1.0);
    final double value = provider.nextDouble(1.0, upper);
    assertEquals(1.0, value);
  }

  @Test
  void shouldHandleFiniteDoubleBoundsWhoseDifferenceOverflowsWithXoroshiro() {
    final XoroshiroRandomProvider provider = new XoroshiroRandomProvider(0L);
    for (int sample = 0; sample < 100; sample++) {
      final double value = provider.nextDouble(-Double.MAX_VALUE, Double.MAX_VALUE);
      final boolean finite = Double.isFinite(value);
      assertTrue(finite);
      final boolean inRange = value >= -Double.MAX_VALUE && value < Double.MAX_VALUE;
      assertTrue(inRange);
    }
  }

  @Test
  void shouldRejectNonfiniteDoubleBoundsWithXoroshiro() {
    final XoroshiroRandomProvider provider = new XoroshiroRandomProvider(0L);
    assertThrows(IllegalArgumentException.class, () -> provider.nextDouble(Double.NEGATIVE_INFINITY, 1.0));
    assertThrows(IllegalArgumentException.class, () -> provider.nextDouble(0.0, Double.POSITIVE_INFINITY));
    assertThrows(IllegalArgumentException.class, () -> provider.nextDouble(Double.NaN, 1.0));
    assertThrows(IllegalArgumentException.class, () -> provider.nextDouble(0.0, Double.NaN));
  }

  @Test
  void shouldSeedUnseededGeneratorsDifferentlyWithXoroshiro() {
    final XoroshiroRandomProvider first = new XoroshiroRandomProvider();
    final XoroshiroRandomProvider second = new XoroshiroRandomProvider();
    final double firstValue = first.nextDouble(0.0, 1.0);
    final double secondValue = second.nextDouble(0.0, 1.0);
    final boolean notANumber = Double.isNaN(firstValue);
    assertNotEquals(firstValue, secondValue);
    assertThrows(IllegalArgumentException.class, () -> first.nextInt(1, 1));
    assertThrows(IllegalArgumentException.class, () -> first.nextDouble(1.0, 1.0));
    assertFalse(notANumber);
  }

  @Test
  void shouldDrawNoiseUpToTheWeightInBothDirectionsWhenRandomDithering() {
    final RandomNumberProvider provider = mock(RandomNumberProvider.class);
    when(provider.nextInt(-3, 4)).thenReturn(3);
    final RandomDitherImpl random = new RandomDitherImpl(DitherTestImages.BLACK_WHITE, 3, provider);
    final int[] pixels = { 0xFF7D7D7D };
    random.dither(pixels, 1);
    verify(provider, times(3)).nextInt(-3, 4);
    assertArrayEquals(new int[] { 0xFFFFFFFF }, pixels, "125 plus the largest noise of 3 reaches the white half");
  }

  @Test
  void shouldMatchAnIndependentReferenceImplementationWithXoroshiro() {
    // computed with a separate implementation of the reference C code of SplitMix64 and xoroshiro128+ (24, 16, 37):
    // the state is the first two outputs of SplitMix64 seeded with 42, and each value is the top 53 bits of an output
    final XoroshiroRandomProvider provider = new XoroshiroRandomProvider(42L);
    final long[] expected = { 8119767394961995L, 693600059381773L, 4769685089197697L };
    for (final long expectedBits : expected) {
      final double value = provider.nextDouble(0.0, 1.0);
      final long bits = (long) (value * 0x1.0p53);
      assertEquals(expectedBits, bits);
    }
  }
}
