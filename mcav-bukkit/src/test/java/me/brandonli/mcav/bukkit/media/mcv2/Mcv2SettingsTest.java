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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import me.brandonli.mcav.bukkit.media.mcv2.Mcv2.Settings;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

final class Mcv2SettingsTest {

  private static final int LAMBDA = 72;

  @Test
  void shouldSearchQuarterSizeMotionOnlyInTheDefaultPreset() throws ReflectiveOperationException {
    for (final Settings settings : new Settings[] { Settings.DEFAULT, Settings.FAST }) {
      final Mcv2 encoder = new Mcv2(settings, ForkJoinPool.commonPool(), 1, true);
      final Field factory = Mcv2.class.getDeclaredField("kernels");
      factory.setAccessible(true);
      final Supplier<?> original = (Supplier<?>) factory.get(encoder);
      final Set<Integer> widths = new HashSet<>();
      final Class<?> contract = Mcv2Internals.nested("Kernels");
      factory.set(encoder, (Supplier<?>) () -> this.recordingKernels(original.get(), contract, widths));
      encoder.encode(Mcv2Pictures.scene(128, 96, 0, 3), 128, 96, 0);
      encoder.encode(Mcv2Pictures.scene(128, 96, 2, 3), 128, 96, 1);
      final Set<Integer> expected = settings.fast() ? Set.of(64, 128) : Set.of(32, 64, 128);
      assertEquals(expected, widths);
    }
  }

  private Object recordingKernels(final Object delegate, final Class<?> contract, final Set<Integer> widths) {
    return Proxy.newProxyInstance(contract.getClassLoader(), new Class<?>[] { contract }, (_, method, arguments) ->
      this.invokeRecordingWidths(delegate, widths, method, arguments)
    );
  }

  private Object invokeRecordingWidths(final Object delegate, final Set<Integer> widths, final Method method, final Object[] arguments)
    throws ReflectiveOperationException {
    if (method.getName().equals("seeded")) {
      widths.add((Integer) arguments[1]);
    }

    method.setAccessible(true);
    return method.invoke(delegate, arguments);
  }

  @Test
  void shouldTryPatternsInPredictedFramesInBothPresets() throws Mcv2Exception {
    final byte[] stripes = new byte[8 * 8 * 3];
    for (int pixel = 0; pixel < 8 * 8; pixel++) {
      Arrays.fill(stripes, pixel * 3, pixel * 3 + 3, (byte) ((pixel & 1) == 0 ? 16 : 240));
    }

    for (final Settings settings : new Settings[] { Settings.DEFAULT, Settings.FAST }) {
      final Mcv2 encoder = new Mcv2(settings, ForkJoinPool.commonPool(), 3, true);
      encoder.encode(new byte[stripes.length], 8, 8, 0);
      final Mcv2Decoder.Frame frame = Mcv2Decoder.parse(encoder.encode(stripes, 8, 8, 1));
      final boolean keyframe = frame.isKeyframe();
      assertEquals(false, keyframe);
      final int leafCount = frame.getLeafCount();
      assertEquals(1, leafCount);
      final int mode = frame.getLeaf(0).mode();
      assertEquals(Mcv2Decoder.MODE_PATTERN, mode);
      final byte @Nullable [] reference = encoder.getReference();
      assertArrayEquals(stripes, reference);
    }
  }

  @Test
  void shouldPinThePresetSearchThresholds() {
    final Object rootSplitBits = Mcv2Internals.field(Mcv2.class, null, "ROOT_SPLIT_BITS");
    assertEquals(150, rootSplitBits);

    final Object normalSteadySplitBits = Mcv2Internals.field(Mcv2.class, null, "NORMAL_STEADY_SPLIT_BITS");
    assertEquals(450, normalSteadySplitBits);

    final Object fastSteadySplitBits = Mcv2Internals.field(Mcv2.class, null, "FAST_STEADY_SPLIT_BITS");
    assertEquals(900, fastSteadySplitBits);

    final Object normalFineSplitBits = Mcv2Internals.field(Mcv2.class, null, "NORMAL_FINE_SPLIT_BITS");
    assertEquals(300, normalFineSplitBits);

    final Object fastFineSplitBits = Mcv2Internals.field(Mcv2.class, null, "FAST_FINE_SPLIT_BITS");
    assertEquals(600, fastFineSplitBits);

    final Object normalSkipBits = Mcv2Internals.field(Mcv2.class, null, "NORMAL_SKIP_BITS");
    assertEquals(28, normalSkipBits);

    final Object fastSkipBits = Mcv2Internals.field(Mcv2.class, null, "FAST_SKIP_BITS");
    assertEquals(60, fastSkipBits);
  }

  @Test
  void shouldKeepTheTwoLivePresetsAsData() {
    final Settings expectedDefault = new Settings(LAMBDA, false);
    assertEquals(expectedDefault, Settings.DEFAULT);

    final Settings expectedFast = new Settings(55, true);
    assertEquals(expectedFast, Settings.FAST);
  }

  @Test
  void shouldCopyWithOneValueChanged() {
    final Settings settings = new Settings(LAMBDA, true);
    final Settings expected = new Settings(0, true);
    final Settings copy = settings.withLambda(0);
    assertEquals(expected, copy);
  }

  @Test
  void shouldRefuseInvalidValues() {
    assertThrows(IllegalArgumentException.class, () -> Settings.DEFAULT.withLambda(-1));
    assertThrows(IllegalArgumentException.class, () -> Settings.DEFAULT.withLambda(Double.POSITIVE_INFINITY));
    assertThrows(IllegalArgumentException.class, () -> Settings.DEFAULT.withLambda(Double.NaN));
  }
}
