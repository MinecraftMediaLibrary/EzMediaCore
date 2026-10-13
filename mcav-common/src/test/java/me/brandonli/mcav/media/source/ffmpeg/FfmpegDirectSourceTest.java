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
package me.brandonli.mcav.media.source.ffmpeg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import me.brandonli.mcav.media.source.SourceDetector;
import me.brandonli.mcav.testing.EqualityAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link FfmpegDirectSource}, {@link DefaultFfmpegDirectSource} and {@link FfmpegDirectSourceDetector}.
 */
final class FfmpegDirectSourceTest {

  @Test
  void shouldDescribeTheInput() {
    final FfmpegDirectSource source = FfmpegDirectSource.mrl("desktop", "gdigrab");
    final String mrl = source.getMrl();
    final String format = source.getFormat();
    final String resource = source.getResource();
    final String name = source.getName();
    final boolean isStatic = source.isStatic();
    final boolean isDynamic = source.isDynamic();
    final String text = source.toString();
    assertEquals("desktop", mrl);
    assertEquals("gdigrab", format);
    assertEquals("desktop", resource);
    assertEquals("ffmpeg", name);
    assertFalse(isStatic, "screen capture and the other raw inputs are live");
    assertTrue(isDynamic);
    assertEquals("FfmpegDirectSource[gdigrab||desktop]", text);
  }

  @Test
  void shouldFollowTheEqualityContract() {
    final FfmpegDirectSource source = FfmpegDirectSource.mrl("desktop", "gdigrab");
    final FfmpegDirectSource equalSource = FfmpegDirectSource.mrl("desktop", "gdigrab");
    final FfmpegDirectSource otherMrl = FfmpegDirectSource.mrl("title=Game", "gdigrab");
    final FfmpegDirectSource otherFormat = FfmpegDirectSource.mrl("desktop", "dshow");
    EqualityAssertions.assertEqualityContract(source, equalSource, otherMrl, otherFormat);
  }

  @Test
  void shouldHashTheMrlAndTheFormat() {
    final FfmpegDirectSource source = FfmpegDirectSource.mrl("desktop", "gdigrab");

    final int hash = source.hashCode();
    final int expected = Objects.hash("desktop", "gdigrab");
    assertEquals(expected, hash);
  }

  @Test
  void shouldRejectInvalidArguments() {
    assertThrows(IllegalArgumentException.class, () -> FfmpegDirectSource.mrl("desktop", " "));
    assertThrows(NullPointerException.class, () -> FfmpegDirectSource.mrl(null, "gdigrab"));
    assertThrows(NullPointerException.class, () -> FfmpegDirectSource.mrl("desktop", null));
  }

  @Test
  void shouldDetectFormatAndInputPairs() {
    final FfmpegDirectSourceDetector detector = new FfmpegDirectSourceDetector();
    final boolean detected = detector.isDetectedSource("dshow||video=Camera");
    final FfmpegDirectSource created = detector.createSource("dshow||video=Camera");
    final FfmpegDirectSource expected = FfmpegDirectSource.mrl("video=Camera", "dshow");
    final int priority = detector.getPriority();
    assertTrue(detected);
    assertEquals(expected, created);
    assertEquals(SourceDetector.HIGH_PRIORITY, priority);
  }

  @ParameterizedTest
  @ValueSource(strings = { "plain", "format||", "||input", " ||input", "a||b||c" })
  void shouldIgnoreTextThatIsNotAPair(final String raw) {
    final FfmpegDirectSourceDetector detector = new FfmpegDirectSourceDetector();
    final boolean detected = detector.isDetectedSource(raw);
    assertFalse(detected);
  }

  @Test
  void shouldRejectInvalidInputInTheDetector() {
    final FfmpegDirectSourceDetector detector = new FfmpegDirectSourceDetector();
    assertThrows(IllegalArgumentException.class, () -> detector.createSource("no separator"));
    assertThrows(NullPointerException.class, () -> detector.isDetectedSource(null));
    assertThrows(NullPointerException.class, () -> detector.createSource(null));
  }
}
