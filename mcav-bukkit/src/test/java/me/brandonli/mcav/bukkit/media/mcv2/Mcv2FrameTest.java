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

import static me.brandonli.mcav.bukkit.media.mcv2.Mcv2Decoder.MODE_SKIP;
import static me.brandonli.mcav.bukkit.media.mcv2.Mcv2Decoder.MODE_SOLID;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import me.brandonli.mcav.bukkit.media.mcv2.Mcv2.Settings;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

final class Mcv2FrameTest {

  private static final ForkJoinPool POOL = ForkJoinPool.commonPool();
  private static final int LIMIT_TRIES = 5;

  private static byte[] panned(final byte[] rgb, final int width, final int height, final int panX) {
    final byte[] out = new byte[rgb.length];
    for (int row = 0; row < height; row++) {
      for (int column = 0; column < width; column++) {
        System.arraycopy(rgb, (row * width + Math.floorMod(column + panX, width)) * 3, out, (row * width + column) * 3, 3);
      }
    }
    return out;
  }

  private static Mcv2 encoder(final Settings settings) {
    return new Mcv2(settings, POOL, 3, true);
  }

  private static byte[] scene(final int width, final int height, final int frame, final int pan) {
    return Mcv2Pictures.scene(width, height, frame, pan);
  }

  private static final class Client {

    private final Mcv2Receiver receiver = new Mcv2Receiver();

    private byte[] decode(final byte[] data) throws Mcv2Exception {
      return Objects.requireNonNull(this.receiver.accept(data));
    }
  }

  private static Mcv2 play(final Settings settings, final int width, final int height, final int frames, final int pan)
    throws Mcv2Exception {
    final Mcv2 encoder = encoder(settings);
    final Client client = new Client();
    for (int index = 0; index < frames; index++) {
      final byte[] data = encoder.encode(scene(width, height, index, pan), width, height, index);
      final byte[] expected = client.decode(data);
      final byte @Nullable [] reference = encoder.getReference();
      assertArrayEquals(expected, reference);
      final long frameId = Mcv2Decoder.parse(data).getFrameId();
      assertEquals(index, frameId);
      final int bytes = Objects.requireNonNull(encoder.getStatistics()).bytes();
      assertEquals(data.length, bytes);
      final boolean expectedKeyframe = index == 0;
      final boolean keyframe = encoder.getStatistics().keyframe();
      assertEquals(expectedKeyframe, keyframe);
      final boolean hasLeaves = encoder.getStatistics().leaves() > 0;
      assertTrue(hasLeaves);
      final boolean timed = encoder.getStatistics().nanoseconds() > 0;
      assertTrue(timed);
    }
    return encoder;
  }

  private static byte[] texture(final int width, final int height, final int seed) {
    final Random random = new Random(seed);
    final byte[] rgb = new byte[width * height * 3];
    for (int row = 0; row < height; row++) {
      for (int column = 0; column < width; column++) {
        final double wave = 45 * Math.sin((column + seed) / 6.0) * Math.cos(row / 5.0) + 25 * Math.sin((column - 2 * row) / 9.0);
        for (int channel = 0; channel < 3; channel++) {
          rgb[(row * width + column) * 3 + channel] = (byte) Math.min(
            255,
            Math.max(0, (int) (120 + wave + 25 * channel + random.nextInt(16)))
          );
        }
      }
    }
    return rgb;
  }

  private static boolean keyframeAfter(final Mcv2 encoder, final byte[] next, final int width, final int height) {
    encoder.encode(next, width, height, 1);
    return encoder.getStatistics().keyframe();
  }

  private static byte[] noise(final int width, final int height, final long seed) {
    final byte[] rgb = new byte[width * height * 3];
    new Random(seed).nextBytes(rgb);
    return rgb;
  }

  @Test
  void shouldKeepTheSettingsItWasMadeWith() {
    final Settings fastSettings = encoder(Settings.FAST).getSettings();
    assertSame(Settings.FAST, fastSettings);
    final Settings defaultSettings = encoder(Settings.DEFAULT).getSettings();
    assertSame(Settings.DEFAULT, defaultSettings);
  }

  @Test
  void shouldRefuseInvalidArguments() {
    assertThrows(IllegalArgumentException.class, () -> new Mcv2(Settings.DEFAULT, POOL, 0, true));
    assertThrows(NullPointerException.class, () -> new Mcv2(null, POOL, 1, true));
    assertThrows(NullPointerException.class, () -> new Mcv2(Settings.DEFAULT, null, 1, true));
    final Mcv2 encoder = encoder(Settings.DEFAULT);
    assertThrows(NullPointerException.class, () -> encoder.encode(null, 8, 8, 0));
    for (final int[] size : new int[][] { { 0, 8 }, { 4097, 8 }, { 8, 0 }, { 8, 4097 } }) {
      assertThrows(IllegalArgumentException.class, () -> encoder.encode(new byte[Math.max(0, size[0] * size[1] * 3)], size[0], size[1], 0));
    }
    assertThrows(IllegalArgumentException.class, () -> encoder.encode(new byte[8 * 8 * 3 - 1], 8, 8, 0));
    assertThrows(IllegalArgumentException.class, () -> encoder.encode(new byte[8 * 8 * 3], 8, 8, -1));
    assertThrows(IllegalArgumentException.class, () -> encoder.encode(new byte[8 * 8 * 3], 8, 8, 1L << 32));
    encoder.encode(new byte[8 * 8 * 3], 8, 8, 5);
    // the same id again, an older one, and one so far ahead that it could be older
    assertThrows(IllegalArgumentException.class, () -> encoder.encode(new byte[8 * 8 * 3], 8, 8, 5));
    assertThrows(IllegalArgumentException.class, () -> encoder.encode(new byte[8 * 8 * 3], 8, 8, 4));
    assertThrows(IllegalArgumentException.class, () -> encoder.encode(new byte[8 * 8 * 3], 8, 8, 5 + 0x80000000L));
    final Mcv2 wrapping = encoder(Settings.DEFAULT);
    wrapping.encode(new byte[8 * 8 * 3], 8, 8, 0xFFFFFFFFL);
    wrapping.encode(new byte[8 * 8 * 3], 8, 8, 0);
    final boolean keyframe = wrapping.getStatistics().keyframe();
    assertFalse(keyframe);
  }

  @Test
  void shouldStartWithAKeyframeAndPredictWhatFollows() throws Mcv2Exception {
    final Mcv2 encoder = encoder(Settings.DEFAULT);
    final Mcv2.@Nullable Statistics statistics = encoder.getStatistics();
    assertNull(statistics);
    final byte @Nullable [] reference = encoder.getReference();
    assertNull(reference);

    final Client client = new Client();
    final byte[] picture = texture(64, 64, 1);
    final byte[] first = encoder.encode(picture, 64, 64, 10);
    final Mcv2.Statistics key = encoder.getStatistics();
    assertNotNull(key);
    final boolean keyframe = key.keyframe();
    assertTrue(keyframe);
    final int bytes = key.bytes();
    assertEquals(first.length, bytes);
    final boolean timed = key.nanoseconds() > 0;
    assertTrue(timed);
    final byte[] expectedFirst = client.decode(first);
    final byte @Nullable [] firstReference = encoder.getReference();
    assertArrayEquals(expectedFirst, firstReference);

    final byte[] second = encoder.encode(picture, 64, 64, 11);
    final Mcv2.Statistics still = encoder.getStatistics();
    final boolean stillKeyframe = still.keyframe();
    assertFalse(stillKeyframe);
    final boolean smaller = second.length < first.length;
    assertTrue(smaller);
    final long referenceId = Mcv2Decoder.parse(second).getReferenceId();
    assertEquals(10, referenceId);
    final byte[] expectedSecond = client.decode(second);
    final byte @Nullable [] secondReference = encoder.getReference();
    assertArrayEquals(expectedSecond, secondReference);
    final byte @Nullable [] oneCopy = encoder.getReference();
    final byte @Nullable [] otherCopy = encoder.getReference();
    assertNotSame(oneCopy, otherCopy);
  }

  @Test
  void shouldStartAgainWithAKeyframeWhenPredictionCannotWork() {
    final byte[] picture = texture(64, 32, 2);
    final Mcv2 interval = encoder(new Settings(72, false));
    for (int id = 0; id < 120; id++) {
      interval.encode(picture, 64, 32, id);
      final boolean expectedKeyframe = id == 0;
      final boolean keyframe = interval.getStatistics().keyframe();
      assertEquals(expectedKeyframe, keyframe);
    }
    interval.encode(picture, 64, 32, 120);
    final boolean keyframeAtInterval = interval.getStatistics().keyframe();
    assertTrue(keyframeAtInterval);

    final Mcv2 wider = encoder(Settings.DEFAULT);
    wider.encode(picture, 64, 32, 0);
    final boolean keyframeWhenWider = keyframeAfter(wider, texture(32, 64, 2), 32, 64);
    assertTrue(keyframeWhenWider);

    final Mcv2 taller = encoder(Settings.DEFAULT);
    taller.encode(picture, 64, 32, 0);
    final boolean keyframeWhenTaller = keyframeAfter(taller, texture(64, 64, 2), 64, 64);
    assertTrue(keyframeWhenTaller);
  }

  @Test
  void shouldStartAgainWithAKeyframeOnRequest() {
    final byte[] picture = texture(64, 32, 2);
    final Mcv2 encoder = encoder(Settings.DEFAULT);
    encoder.encode(picture, 64, 32, 0);
    encoder.requestKeyframe();
    final boolean keyframe = keyframeAfter(encoder, picture, 64, 32);
    assertTrue(keyframe);
  }

  @Test
  void shouldEncodePicturesThatAreNotWholeBlocks() throws Mcv2Exception {
    final Client client = new Client();
    final Mcv2 encoder = encoder(Settings.DEFAULT);
    final byte[] picture = texture(40, 20, 5);
    final byte[] expectedPicture = client.decode(encoder.encode(picture, 40, 20, 0));
    final byte @Nullable [] pictureReference = encoder.getReference();
    assertArrayEquals(expectedPicture, pictureReference);
    final byte[] expectedPanned = client.decode(encoder.encode(panned(picture, 40, 20, 3), 40, 20, 1));
    final byte @Nullable [] pannedReference = encoder.getReference();
    assertArrayEquals(expectedPanned, pannedReference);

    final Mcv2 tiny = encoder(Settings.DEFAULT);
    final byte[] expectedTiny = new Client().decode(tiny.encode(new byte[] { 1, 2, 3 }, 1, 1, 0));
    final byte @Nullable [] tinyReference = tiny.getReference();
    assertArrayEquals(expectedTiny, tinyReference);
  }

  @Test
  void shouldWriteTheSameBytesWithoutVerification() {
    final byte[] picture = texture(64, 32, 6);
    final byte[] next = panned(picture, 64, 32, 2);
    final Mcv2 checked = new Mcv2(Settings.DEFAULT, POOL, 1, true);
    final Mcv2 unchecked = new Mcv2(Settings.DEFAULT, POOL, 3, false);
    final byte[] expectedFirst = checked.encode(picture, 64, 32, 0);
    final byte[] uncheckedFirst = unchecked.encode(picture, 64, 32, 0);
    assertArrayEquals(expectedFirst, uncheckedFirst);
    final byte[] expectedNext = checked.encode(next, 64, 32, 1);
    final byte[] uncheckedNext = unchecked.encode(next, 64, 32, 1);
    assertArrayEquals(expectedNext, uncheckedNext);
  }

  @Test
  void shouldDecodeEveryFrameOfTheLiveProfile() throws Mcv2Exception {
    play(Settings.DEFAULT, 100, 70, 6, 2);
    play(Settings.DEFAULT, 64, 64, 4, 0);
  }

  @Test
  void shouldRaiseTheLambdaOfFastMotion() throws Mcv2Exception {
    final Settings settings = Settings.DEFAULT;
    // noise that changes from frame to frame but no movement keeps the profile's lambda, which is all a search without
    // the motion's lambda uses
    final double steadyLambda = play(settings, 64, 64, 4, 0).getStatistics().lambda();
    assertEquals(72, steadyLambda);

    final Mcv2 pan = play(settings, 100, 70, 6, 9);
    final boolean raised = pan.getStatistics().lambda() > 72;
    assertTrue(raised);
  }

  @Test
  void shouldDecodeEveryFrameOfTheQuarterResolutionSearch() throws Mcv2Exception {
    play(Settings.DEFAULT, 100, 70, 5, 5);
    play(Settings.DEFAULT, 64, 64, 4, 2);
    play(Settings.FAST, 100, 70, 4, 3);
  }

  @Test
  void shouldStartAgainAfterASizeChange() throws Mcv2Exception {
    final Mcv2 encoder = new Mcv2(Settings.DEFAULT, POOL, 2, true);
    final Client client = new Client();
    client.decode(encoder.encode(scene(64, 64, 0, 0), 64, 64, 0));
    final byte[] inverted = scene(64, 64, 0, 0);
    for (int index = 0; index < inverted.length; index++) {
      inverted[index] = (byte) (255 - (inverted[index] & 0xFF));
    }
    final byte[] expectedInverted = client.decode(encoder.encode(inverted, 64, 64, 1));
    final byte @Nullable [] invertedReference = encoder.getReference();
    assertArrayEquals(expectedInverted, invertedReference);
    final boolean invertedKeyframe = encoder.getStatistics().keyframe();
    assertFalse(invertedKeyframe);

    final byte[] resized = encoder.encode(scene(96, 32, 2, 0), 96, 32, 2);
    final byte[] expectedResized = client.decode(resized);
    final byte @Nullable [] resizedReference = encoder.getReference();
    assertArrayEquals(expectedResized, resizedReference);
    final boolean resizedKeyframe = encoder.getStatistics().keyframe();
    assertTrue(resizedKeyframe);

    final byte[] next = encoder.encode(scene(96, 32, 3, 0), 96, 32, 3);
    final byte[] expectedNext = client.decode(next);
    final byte @Nullable [] nextReference = encoder.getReference();
    assertArrayEquals(expectedNext, nextReference);
    final boolean nextKeyframe = encoder.getStatistics().keyframe();
    assertFalse(nextKeyframe);

    final byte[] taller = encoder.encode(scene(96, 48, 4, 0), 96, 48, 4);
    final byte[] expectedTaller = client.decode(taller);
    final byte @Nullable [] tallerReference = encoder.getReference();
    assertArrayEquals(expectedTaller, tallerReference);
    final boolean tallerKeyframe = encoder.getStatistics().keyframe();
    assertTrue(tallerKeyframe);
  }

  @Test
  void shouldEndAFrameSoonAfterItsBudgetWithTheCheapestChoices() throws Mcv2Exception {
    final Mcv2 hurried = new Mcv2(Settings.DEFAULT, POOL, 2, true);
    assertThrows(IllegalArgumentException.class, () -> hurried.setFrameBudget(-1));
    // a budget every superblock starts past: a keyframe of one solid colour per superblock, SKIP where the colour is
    // the frame's commonest, and a P frame of SKIP everywhere, each decoding to the encoder's own picture
    hurried.setFrameBudget(1);
    final Client client = new Client();
    final Mcv2Decoder.Frame keyframe = Mcv2Decoder.parse(hurried.encode(scene(96, 64, 0, 3), 96, 64, 0));
    final boolean startsWithKeyframe = keyframe.isKeyframe();
    assertTrue(startsWithKeyframe);
    final int keyframeLeafCount = keyframe.getLeafCount();
    assertEquals(6, keyframeLeafCount);
    for (int leafIndex = 0; leafIndex < keyframe.getLeafCount(); leafIndex++) {
      final Mcv2Decoder.Leaf leaf = keyframe.getLeaf(leafIndex);
      final int size = leaf.size();
      assertEquals(32, size);
      final boolean cheapest = leaf.mode() == MODE_SOLID || leaf.mode() == MODE_SKIP;
      assertTrue(cheapest, "mode " + leaf.mode());
    }
    final byte @Nullable [] expectedKeyframe = hurried.getReference();
    final byte[] decodedKeyframe = client.decode(keyframe.getData());
    assertArrayEquals(expectedKeyframe, decodedKeyframe);

    final byte[] next = hurried.encode(scene(96, 64, 1, 3), 96, 64, 1);
    final Mcv2Decoder.Frame frame = Mcv2Decoder.parse(next);
    final int frameLeafCount = frame.getLeafCount();
    assertEquals(6, frameLeafCount);
    for (int leafIndex = 0; leafIndex < frame.getLeafCount(); leafIndex++) {
      final int mode = frame.getLeaf(leafIndex).mode();
      assertEquals(MODE_SKIP, mode);
    }
    final byte @Nullable [] expectedNext = hurried.getReference();
    final byte[] decodedNext = client.decode(next);
    assertArrayEquals(expectedNext, decodedNext);

    final Mcv2 patient = new Mcv2(Settings.DEFAULT, POOL, 2, true);
    patient.setFrameBudget(TimeUnit.HOURS.toNanos(1));
    final Mcv2 unbounded = new Mcv2(Settings.DEFAULT, POOL, 2, true);
    for (int frameNumber = 0; frameNumber < 4; frameNumber++) {
      final byte[] picture = scene(96, 64, frameNumber, 3);
      final byte[] expected = unbounded.encode(picture, 96, 64, frameNumber);
      final byte[] patientFrame = patient.encode(picture, 96, 64, frameNumber);
      assertArrayEquals(expected, patientFrame);
    }
    patient.setFrameBudget(0);
    final byte[] expectedUnbudgeted = unbounded.encode(scene(96, 64, 4, 3), 96, 64, 4);
    final byte[] unbudgetedFrame = patient.encode(scene(96, 64, 4, 3), 96, 64, 4);
    assertArrayEquals(expectedUnbudgeted, unbudgetedFrame);
  }

  @Test
  void shouldSwitchLiveProfilesWithoutAKeyframe() throws Mcv2Exception {
    final Mcv2 encoder = new Mcv2(Settings.DEFAULT, POOL, 2, true);
    final Client client = new Client();
    for (int frameNumber = 0; frameNumber < 8; frameNumber++) {
      if (frameNumber == 3) {
        encoder.switchTo(Settings.FAST);
        final Settings settings = encoder.getSettings();
        assertEquals(Settings.FAST, settings);
      } else if (frameNumber == 6) {
        encoder.switchTo(Settings.DEFAULT);
      }
      final byte[] data = encoder.encode(scene(96, 64, frameNumber, 3), 96, 64, frameNumber);
      final boolean expectedKeyframe = frameNumber == 0;
      final boolean keyframe = Objects.requireNonNull(encoder.getStatistics()).keyframe();
      assertEquals(expectedKeyframe, keyframe, "frame " + frameNumber);
      final byte[] expected = client.decode(data);
      final byte @Nullable [] reference = encoder.getReference();
      assertArrayEquals(expected, reference, "frame " + frameNumber);
    }
    assertThrows(NullPointerException.class, () -> encoder.switchTo(null));
  }

  @Test
  void shouldSearchAFrameOverItsByteBoundAgainAtAHigherLambda() throws Mcv2Exception {
    final byte[] picture = noise(96, 64, 5);
    final byte[][] keyframes = new byte[LIMIT_TRIES][];
    for (int attempt = 0; attempt < LIMIT_TRIES; attempt++) {
      keyframes[attempt] = new Mcv2(Settings.DEFAULT.withLambda(72 << attempt), POOL, 2, false).encode(picture, 96, 64, 0);
    }
    for (final int bound : new int[] { keyframes[0].length, keyframes[0].length - 1, keyframes[2].length, 1 }) {
      int expected = 0;
      while (expected < LIMIT_TRIES - 1 && keyframes[expected].length > bound) {
        expected++;
      }
      final Mcv2 bounded = new Mcv2(Settings.DEFAULT, POOL, 2, true);
      bounded.setFrameLimit(bound);
      final byte[] actual = bounded.encode(picture, 96, 64, 0);
      if (keyframes[expected].length <= bound) {
        final byte[] expectedKeyframe = keyframes[expected];
        assertArrayEquals(expectedKeyframe, actual, "bound " + bound);
      } else {
        final Mcv2 trivial = new Mcv2(Settings.DEFAULT, POOL, 2, true);
        trivial.setFrameBudget(1);
        final byte[] expectedTrivial = trivial.encode(picture, 96, 64, 0);
        assertArrayEquals(expectedTrivial, actual, "bound " + bound);
      }
      final int expectedLambda = 72 << expected;
      final double lambda = Objects.requireNonNull(bounded.getStatistics()).lambda();
      assertEquals(expectedLambda, lambda);

      final Client client = new Client();
      client.decode(actual);
      final byte[] next = bounded.encode(noise(96, 64, 6), 96, 64, 1);
      final byte[] expectedNext = client.decode(next);
      final byte @Nullable [] reference = bounded.getReference();
      assertArrayEquals(expectedNext, reference, "bound " + bound);
    }
    final Mcv2 unbounded = new Mcv2(Settings.DEFAULT, POOL, 2, false);
    unbounded.setFrameLimit(0);
    final byte[] expectedFirstKeyframe = keyframes[0];
    final byte[] unboundedKeyframe = unbounded.encode(picture, 96, 64, 0);
    assertArrayEquals(expectedFirstKeyframe, unboundedKeyframe);
    assertThrows(IllegalArgumentException.class, () -> unbounded.setFrameLimit(-1));
  }
}
