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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

final class MCV2Test {

  @Test
  void shouldWriteTheHandComputedUniformKeyframeAndPredictedGolden() throws Mcv2Exception {
    try (final ForkJoinPool pool = new ForkJoinPool(1)) {
      final MCV2 encoder = new MCV2(MCV2.Settings.DEFAULT, pool, 1, true);
      final byte[] picture = { 10, 20, 30 };
      final byte[] expected = HexFormat.of().parseHex(
        "4d43563203000000010001000100000001000000" + "0100000000000000010000000000000000000000" + "02000000000a141e"
      );
      final byte[] encodedKeyframe = encoder.encode(picture, 1, 1, 1);
      assertArrayEquals(expected, encodedKeyframe);

      final byte[] predicted = HexFormat.of().parseHex(
        "4d43563203000000010001000200000001000000" + "0000000000000000000000000000000000000000"
      );
      final byte[] encodedPredicted = encoder.encode(picture, 1, 1, 2);
      assertArrayEquals(predicted, encodedPredicted);
      final byte[] decoded = Mcv2Decoder.decode(predicted, picture, 1);
      assertArrayEquals(picture, decoded);
      final int bytes = encoder.getStatistics().bytes();
      assertEquals(40, bytes);
      final boolean keyframe = encoder.getStatistics().keyframe();
      assertFalse(keyframe);
      final double lambda = encoder.getStatistics().lambda();
      assertEquals(72, lambda);
    }
  }

  @Test
  void shouldReconstructExactlyWhatDecodeGivesForEveryPresetAndOddSize() throws Mcv2Exception {
    final Random random = new Random(0x4D435632);
    for (final MCV2.Settings settings : new MCV2.Settings[] { MCV2.Settings.DEFAULT, MCV2.Settings.FAST }) {
      try (final ForkJoinPool pool = new ForkJoinPool(4)) {
        final MCV2 encoder = new MCV2(settings, pool, 4, true);
        byte[] reference = null;
        for (int index = 0; index < 12; index++) {
          final byte[] picture = new byte[65 * 37 * 3];
          random.nextBytes(picture);
          final byte[] data = encoder.encode(picture, 65, 37, index);
          final Mcv2Decoder.Frame frame = Mcv2Decoder.parse(data);
          reference = Mcv2Decoder.decode(frame, reference, frame.getReferenceId());
          final byte @Nullable [] encoderReference = encoder.getReference();
          assertArrayEquals(reference, encoderReference);
          final long frameId = frame.getFrameId();
          assertEquals(index, frameId);
          final boolean withinLimit = data.length <= Mcv2Decoder.MAXIMUM_FRAME_BYTES;
          assertTrue(withinLimit);
          if (!frame.isKeyframe()) {
            final int expectedReferenceId = index - 1;
            final long referenceId = frame.getReferenceId();
            assertEquals(expectedReferenceId, referenceId);
          }
        }
      }
    }
  }

  @Test
  void shouldWriteIdenticalBytesOnOneThroughFourThreads() {
    final byte[][] source = new byte[5][128 * 96 * 3];
    final Random random = new Random(7719);
    random.nextBytes(source[0]);
    for (int index = 1; index < source.length; index++) {
      System.arraycopy(source[index - 1], 0, source[index], 0, source[index].length);
      for (int channel = 0; channel < source[index].length; channel += 19) {
        source[index][channel]++;
      }
    }
    for (final MCV2.Settings settings : new MCV2.Settings[] { MCV2.Settings.DEFAULT, MCV2.Settings.FAST }) {
      final byte[][] golden = new byte[source.length][];
      for (int threads = 1; threads <= 4; threads++) {
        try (final ForkJoinPool pool = new ForkJoinPool(threads)) {
          final MCV2 encoder = new MCV2(settings, pool, threads, true);
          for (int index = 0; index < source.length; index++) {
            final byte[] actual = encoder.encode(source[index], 128, 96, index);
            if (threads == 1) {
              golden[index] = actual;
            } else {
              final byte[] expected = golden[index];
              assertArrayEquals(expected, actual);
            }
          }
        }
      }
    }
  }

  @Test
  void shouldRetryAndFallBackFromTheSameReference() throws Mcv2Exception {
    try (final ForkJoinPool pool = new ForkJoinPool(2)) {
      final MCV2 encoder = new MCV2(MCV2.Settings.DEFAULT.withLambda(0), pool, 2, true);
      final byte[] source = new byte[63 * 65 * 3];
      new Random(17).nextBytes(source);
      encoder.setFrameLimit(56);
      final byte[] first = encoder.encode(source, 63, 65, 0);
      final Mcv2Decoder.Frame parsed = Mcv2Decoder.parse(first);
      for (int index = 0; index < parsed.getLeafCount(); index++) {
        final int leafSize = parsed.getLeaf(index).size();
        assertEquals(32, leafSize);
      }
      final boolean withinLimit = first.length <= Mcv2Decoder.MAXIMUM_FRAME_BYTES;
      assertTrue(withinLimit);

      final byte[] reference = encoder.getReference();
      source[0]++;
      final byte[] second = encoder.encode(source, 63, 65, 1);
      final byte @Nullable [] referenceAfterFallback = encoder.getReference();
      assertArrayEquals(reference, referenceAfterFallback);
      final long referenceId = Mcv2Decoder.parse(second).getReferenceId();
      assertEquals(0, referenceId);
      final byte[] decoded = Mcv2Decoder.decode(second, reference, 0);
      assertArrayEquals(reference, decoded);
    }
  }

  @Test
  void shouldRetryAtMostFourTimesWhenEvenTheTrivialFrameExceedsTheLimit() throws Mcv2Exception {
    try (final ForkJoinPool pool = new ForkJoinPool(1)) {
      final MCV2 encoder = new MCV2(MCV2.Settings.DEFAULT.withLambda(1), pool, 1, true);
      encoder.setFrameLimit(40);
      final byte[] source = { 10, 20, 30 };
      final byte[] data = encoder.encode(source, 1, 1, 0);
      final double lambda = encoder.getStatistics().lambda();
      assertEquals(16, lambda);
      final int frameLength = data.length;
      assertEquals(48, frameLength);
      final byte[] decoded = Mcv2Decoder.decode(data, null, 0);
      assertArrayEquals(source, decoded);
    }
  }

  @Test
  void shouldMeasureOnlyTheTimeSpentBeginningAndFinishing() {
    try (final ForkJoinPool pool = new ForkJoinPool(1)) {
      final MCV2 encoder = new MCV2(MCV2.Settings.DEFAULT, pool, 1, true);
      final long started = System.nanoTime();
      final MCV2.Pending pending = encoder.begin(new byte[] { 10, 20, 30 }, 1, 1, 0);
      final long searched = (long) Mcv2Internals.field(MCV2.Pending.class, pending, "nanoseconds");
      final boolean searchWithinElapsed = searched >= 0 && searched <= System.nanoTime() - started;
      assertTrue(searchWithinElapsed);

      final MCV2.Statistics statistics = encoder.finish(pending).getStatistics();
      final boolean measuredWithinElapsed = statistics.nanoseconds() >= searched && statistics.nanoseconds() <= System.nanoTime() - started;
      assertTrue(measuredWithinElapsed);
    }
  }

  @Test
  void shouldUseMeanSolidsAndThenSkipWhenTheBudgetIsExhausted() throws Mcv2Exception {
    try (final ForkJoinPool pool = new ForkJoinPool(2)) {
      final MCV2 encoder = new MCV2(MCV2.Settings.DEFAULT, pool, 2, true);
      encoder.setFrameBudget(1);
      final byte[] source = new byte[32 * 32 * 3];
      for (int pixel = 0; pixel < 32 * 32; pixel++) {
        source[pixel * 3] = (byte) ((pixel % 32) * 2);
      }
      final byte[] first = encoder.encode(source, 32, 32, 0);
      final byte[] decoded = Mcv2Decoder.decode(first, null, 0);
      for (int pixel = 0; pixel < 32 * 32; pixel++) {
        final byte red = decoded[pixel * 3];
        assertEquals(31, red);
      }
      final int firstLength = first.length;
      assertEquals(48, firstLength);

      source[0] = 10;
      final int secondLength = encoder.encode(source, 32, 32, 1).length;
      assertEquals(40, secondLength);
      final byte @Nullable [] reference = encoder.getReference();
      assertArrayEquals(decoded, reference);
    }
  }

  @Test
  void shouldPlaceKeyframesByRequestsSizesIntervalsAndWrappedIds() throws Mcv2Exception {
    try (final ForkJoinPool pool = new ForkJoinPool(1)) {
      final MCV2 encoder = new MCV2(new MCV2.Settings(72, false), pool, 1, true);
      final byte[] black = new byte[3];
      final boolean firstIsKeyframe = Mcv2Decoder.parse(encoder.encode(black, 1, 1, 0xFFFFFFFEL)).isKeyframe();
      assertTrue(firstIsKeyframe);
      final boolean maximumIdIsKeyframe = Mcv2Decoder.parse(encoder.encode(black, 1, 1, 0xFFFFFFFFL)).isKeyframe();
      assertFalse(maximumIdIsKeyframe);
      final boolean wrappedIdIsKeyframe = Mcv2Decoder.parse(encoder.encode(black, 1, 1, 0)).isKeyframe();
      assertFalse(wrappedIdIsKeyframe);
      for (int id = 1; id <= 117; id++) {
        final boolean idIsKeyframe = Mcv2Decoder.parse(encoder.encode(black, 1, 1, id)).isKeyframe();
        assertFalse(idIsKeyframe);
      }
      final boolean intervalIsKeyframe = Mcv2Decoder.parse(encoder.encode(black, 1, 1, 118)).isKeyframe();
      assertTrue(intervalIsKeyframe);

      encoder.requestKeyframe();
      final boolean requestedIsKeyframe = Mcv2Decoder.parse(encoder.encode(black, 1, 1, 119)).isKeyframe();
      assertTrue(requestedIsKeyframe);
      final boolean resizedIsKeyframe = Mcv2Decoder.parse(encoder.encode(new byte[6], 2, 1, 120)).isKeyframe();
      assertTrue(resizedIsKeyframe);

      final byte[] white = new byte[6];
      Arrays.fill(white, (byte) 255);
      final boolean changedIsKeyframe = Mcv2Decoder.parse(encoder.encode(white, 2, 1, 121)).isKeyframe();
      assertFalse(changedIsKeyframe);
      assertThrows(IllegalArgumentException.class, () -> encoder.encode(white, 2, 1, 121));
      assertThrows(IllegalArgumentException.class, () -> encoder.encode(white, 2, 1, 0x80000079L));
    }
  }

  @Test
  void shouldKeepTheReferenceAndMotionLambdaWhenSwitchingSettings() throws Mcv2Exception {
    try (final ForkJoinPool pool = new ForkJoinPool(1)) {
      final MCV2 encoder = new MCV2(MCV2.Settings.DEFAULT, pool, 1, true);
      final byte[] source = new byte[32 * 32 * 3];
      encoder.encode(source, 32, 32, 0);
      Arrays.fill(source, (byte) 10);
      encoder.encode(source, 32, 32, 1);
      Arrays.fill(source, (byte) 20);
      encoder.encode(source, 32, 32, 2);
      final double raised = 72 * Math.pow(10 / 4.6, 0.79);
      final double lambda = encoder.getStatistics().lambda();
      assertEquals(raised, lambda);

      encoder.switchTo(MCV2.Settings.FAST.withLambda(30));
      final MCV2.Settings expectedSettings = MCV2.Settings.FAST.withLambda(30);
      final MCV2.Settings settings = encoder.getSettings();
      assertEquals(expectedSettings, settings);

      final Mcv2Decoder.Frame next = Mcv2Decoder.parse(encoder.encode(source, 32, 32, 3));
      final boolean keyframe = next.isKeyframe();
      assertFalse(keyframe);
      final long referenceId = next.getReferenceId();
      assertEquals(2, referenceId);
      final double expectedLambda = 30 * Math.pow(10 / 4.6, 0.79);
      final double lambdaAfterSwitch = encoder.getStatistics().lambda();
      assertEquals(expectedLambda, lambdaAfterSwitch);
    }
  }

  @Test
  void shouldMatchSerialInThePipelineAndEnforceFinishOrderAndOwnership() {
    try (final ForkJoinPool pool = new ForkJoinPool(2)) {
      final MCV2 serial = new MCV2(MCV2.Settings.DEFAULT, pool, 2, true);
      final MCV2 pipelined = new MCV2(MCV2.Settings.DEFAULT, pool, 2, true);
      final byte[][] source = new byte[3][32 * 32 * 3];
      final Random random = new Random(171);
      for (final byte[] picture : source) {
        random.nextBytes(picture);
      }
      final MCV2.Pending first = pipelined.begin(source[0], 32, 32, 0);
      final MCV2.Pending second = pipelined.begin(source[1], 32, 32, 1);
      assertThrows(IllegalStateException.class, () -> pipelined.begin(source[2], 32, 32, 2));
      assertThrows(IllegalStateException.class, () -> pipelined.finish(second));
      assertThrows(IllegalArgumentException.class, () -> serial.finish(first));
      final byte[] expectedFirst = serial.encode(source[0], 32, 32, 0);
      final byte[] pipelinedFirst = pipelined.finish(first).getData();
      assertArrayEquals(expectedFirst, pipelinedFirst);

      final MCV2.Pending third = pipelined.begin(source[2], 32, 32, 2);
      final byte[] expectedSecond = serial.encode(source[1], 32, 32, 1);
      final byte[] pipelinedSecond = pipelined.finish(second).getData();
      assertArrayEquals(expectedSecond, pipelinedSecond);
      final byte[] expectedThird = serial.encode(source[2], 32, 32, 2);
      final byte[] pipelinedThird = pipelined.finish(third).getData();
      assertArrayEquals(expectedThird, pipelinedThird);
      assertThrows(IllegalStateException.class, () -> pipelined.finish(third));
    }
  }

  @Test
  void shouldPermanentlyStopBothPipelineStagesOnAVerificationFailure() throws ReflectiveOperationException {
    try (final ForkJoinPool pool = new ForkJoinPool(1)) {
      final MCV2 encoder = new MCV2(MCV2.Settings.DEFAULT, pool, 1, true);
      final MCV2.Pending first = encoder.begin(new byte[3], 1, 1, 0);
      final MCV2.Pending second = encoder.begin(new byte[3], 1, 1, 1);
      final Field picture = MCV2.Pending.class.getDeclaredField("picture");
      picture.setAccessible(true);
      ((byte[]) picture.get(first))[0] = 1;
      assertThrows(IllegalStateException.class, () -> encoder.finish(first));
      assertThrows(IllegalStateException.class, () -> encoder.finish(second));
      assertThrows(IllegalStateException.class, () -> encoder.begin(new byte[3], 1, 1, 2));
      final MCV2.@Nullable Statistics statistics = encoder.getStatistics();
      assertNull(statistics);
    }
  }

  @Test
  void shouldCheckSettingsInputsAndReferenceOwnership() {
    try (final ForkJoinPool pool = new ForkJoinPool(1)) {
      assertThrows(IllegalArgumentException.class, () -> new MCV2.Settings(-1, false));
      assertThrows(IllegalArgumentException.class, () -> new MCV2.Settings(Double.NaN, false));
      assertThrows(IllegalArgumentException.class, () -> new MCV2.Settings(Double.POSITIVE_INFINITY, false));
      assertThrows(IllegalArgumentException.class, () -> new MCV2(MCV2.Settings.DEFAULT, pool, 0, false));
      assertThrows(NullPointerException.class, () -> new MCV2(null, pool, 1, false));
      assertThrows(NullPointerException.class, () -> new MCV2(MCV2.Settings.DEFAULT, null, 1, false));
      final MCV2 encoder = new MCV2(MCV2.Settings.DEFAULT, pool, 1, false);
      final byte @Nullable [] initialReference = encoder.getReference();
      assertNull(initialReference);
      final MCV2.@Nullable Statistics statistics = encoder.getStatistics();
      assertNull(statistics);
      assertThrows(IllegalArgumentException.class, () -> encoder.setFrameBudget(-1));
      assertThrows(IllegalArgumentException.class, () -> encoder.setFrameLimit(-1));
      assertThrows(IllegalArgumentException.class, () -> encoder.encode(new byte[3], 0, 1, 0));
      final IllegalArgumentException dimensionsException = assertThrows(IllegalArgumentException.class, () ->
        encoder.encode(new byte[3], 4097, 1, 0)
      );
      final String dimensionsMessage = dimensionsException.getMessage();
      assertEquals("Invalid dimensions!", dimensionsMessage);
      assertThrows(IllegalArgumentException.class, () -> encoder.encode(new byte[2], 1, 1, 0));
      final IllegalArgumentException sizeException = assertThrows(IllegalArgumentException.class, () ->
        encoder.encode(new byte[4], 1, 1, 0)
      );
      final String sizeMessage = sizeException.getMessage();
      assertEquals("Picture size does not match the dimensions!", sizeMessage);
      assertThrows(IllegalArgumentException.class, () -> encoder.encode(new byte[3], 1, 1, -1));
      assertThrows(IllegalArgumentException.class, () -> encoder.encode(new byte[3], 1, 1, 0x100000000L));
      assertThrows(NullPointerException.class, () -> encoder.encode(null, 1, 1, 0));
      assertThrows(NullPointerException.class, () -> encoder.switchTo(null));
      assertThrows(NullPointerException.class, () -> encoder.finish(null));

      encoder.encode(new byte[3], 1, 1, 0);
      assertThrows(IllegalArgumentException.class, () -> encoder.begin(new byte[3], 1, 1, 0));
      assertThrows(IllegalArgumentException.class, () -> encoder.begin(new byte[3], 1, 1, 0x80000000L));
      final byte[] reference = encoder.getReference();
      assertNotNull(reference);

      reference[0] = 42;
      final byte[] expected = new byte[3];
      final byte @Nullable [] referenceAfterWrite = encoder.getReference();
      assertArrayEquals(expected, referenceAfterWrite);
    }
  }
}
