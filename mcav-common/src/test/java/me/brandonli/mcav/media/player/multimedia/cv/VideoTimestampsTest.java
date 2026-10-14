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
package me.brandonli.mcav.media.player.multimedia.cv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

final class VideoTimestampsTest {

  private static final double FRAMES_PER_SECOND = 30.0;

  private final AtomicLong nanoseconds = new AtomicLong(1_000_000_000L);

  @Test
  void shouldKeepTimestampsThatComeAfterThePreviousFrame() {
    final VideoTimestamps timestamps = new VideoTimestamps(FRAMES_PER_SECOND, false, this.nanoseconds::get);
    final long first = timestamps.next(5_000L);
    final long second = timestamps.next(38_333L);
    final long jump = timestamps.next(9_000_000L);
    assertEquals(5_000L, first);
    assertEquals(38_333L, second);
    assertEquals(9_000_000L, jump);
  }

  @Test
  void shouldAdvanceMediaOfAKnownLengthByOneFramePeriod() {
    final VideoTimestamps timestamps = new VideoTimestamps(25.0, false, this.nanoseconds::get);
    final long first = timestamps.next(0L);
    final long second = timestamps.next(0L);
    final long third = timestamps.next(0L);
    final long backwards = timestamps.next(10_000L);
    assertEquals(0L, first);
    assertEquals(40_000L, second, "a frame period of 25 frames per second is 40 ms");
    assertEquals(80_000L, third);
    assertEquals(120_000L, backwards, "a timestamp before the previous frame is derived as well");
  }

  @Test
  void shouldUseTheArrivalTimeOfLiveFramesAndOfMediaWithoutAFrameRate() {
    final VideoTimestamps live = new VideoTimestamps(FRAMES_PER_SECOND, true, this.nanoseconds::get);
    final VideoTimestamps noRate = new VideoTimestamps(0.0, false, this.nanoseconds::get);
    final VideoTimestamps infiniteRate = new VideoTimestamps(Double.POSITIVE_INFINITY, false, this.nanoseconds::get);
    live.next(0L);
    noRate.next(0L);
    infiniteRate.next(0L);
    this.nanoseconds.addAndGet(50_000_000L);
    final long liveSecond = live.next(0L);
    final long noRateSecond = noRate.next(0L);
    final long infiniteSecond = infiniteRate.next(0L);
    final long sameMoment = live.next(0L);
    assertEquals(50_000L, liveSecond, "the frame arrived 50 ms after the first one");
    assertEquals(50_000L, noRateSecond);
    assertEquals(50_000L, infiniteSecond);
    assertEquals(50_001L, sameMoment, "time still moves forward for frames arriving at the same moment");
  }
}
