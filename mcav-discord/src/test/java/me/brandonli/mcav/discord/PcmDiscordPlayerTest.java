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
package me.brandonli.mcav.discord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import me.brandonli.mcav.json.GsonProvider;
import me.brandonli.mcav.json.ytdlp.format.UrlParseDump;
import me.brandonli.mcav.media.player.metadata.OriginalAudioMetadata;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.Presence;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

/**
 * Tests {@link PcmDiscordPlayer} and the {@link DiscordPlayer} factory.
 */
final class PcmDiscordPlayerTest {

  private static final int FRAME_BYTES = 3840;
  private static final int FRAME_MILLISECONDS = 20;
  private static final int MAXIMUM_FRAMES = 150;

  private static final int PRODUCERS = 4;
  private static final int FRAMES_PER_PRODUCER = 25;
  private static final long PRODUCER_TIMEOUT_SECONDS = 10;

  private static final int MAXIMUM_NAME_LENGTH = 128;
  private static final String UNKNOWN_TITLE = "Unknown title";
  private static final String ELLIPSIS = "…";

  private final JDA jda = Mockito.mock(JDA.class);
  private final Presence presence = Mockito.mock(Presence.class);
  private final OriginalAudioMetadata metadata = Mockito.mock(OriginalAudioMetadata.class);

  private PcmDiscordPlayer createPlayer() {
    Mockito.when(this.jda.getPresence()).thenReturn(this.presence);
    return new PcmDiscordPlayer(this.jda);
  }

  private static ByteBuffer filledBuffer(final int size, final byte value) {
    final byte[] bytes = new byte[size];
    Arrays.fill(bytes, value);
    final ByteBuffer buffer = ByteBuffer.wrap(bytes);
    buffer.order(ByteOrder.BIG_ENDIAN);
    return buffer;
  }

  private static byte[] contentOf(final ByteBuffer frame) {
    final int remaining = frame.remaining();
    final byte[] bytes = new byte[remaining];
    frame.get(bytes);
    return bytes;
  }

  /**
   * Captures every activity shown so far.
   *
   * @param mode how often the activity must have been set
   * @return the shown activities, oldest first
   */
  private List<Activity> shownActivities(final VerificationMode mode) {
    final ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
    final Presence verified = Mockito.verify(this.presence, mode);
    final Activity captured = captor.capture();
    verified.setActivity(captured);
    return captor.getAllValues();
  }

  private String shownActivityName() {
    final VerificationMode atLeastOnce = Mockito.atLeastOnce();
    final List<Activity> activities = this.shownActivities(atLeastOnce);
    final Activity activity = activities.getLast();
    final Activity.ActivityType type = activity.getType();
    assertEquals(Activity.ActivityType.PLAYING, type);
    return activity.getName();
  }

  /**
   * Asserts that a frame holds one value up to a split index and another value from there on.
   */
  private static void assertFrameHolds(final ByteBuffer frame, final byte before, final int split, final byte after) {
    assertNotNull(frame);
    final byte[] bytes = contentOf(frame);
    final byte first = bytes[0];
    final byte lastBeforeSplit = bytes[split - 1];
    final byte firstAfterSplit = bytes[split];
    final byte last = bytes[FRAME_BYTES - 1];
    assertEquals(before, first);
    assertEquals(before, lastBeforeSplit);
    assertEquals(after, firstAfterSplit);
    assertEquals(after, last);
  }

  @Test
  void shouldHoldTwentyMillisecondsOfStereoSixteenBitAudioInAFrame() {
    final int frameBytes = PcmDiscordPlayer.FRAME_BYTES;
    assertEquals(FRAME_BYTES, frameBytes);
  }

  @Test
  void shouldCreateTheDefaultPlayerAndRejectNull() {
    final DiscordPlayer player = DiscordPlayer.voice(this.jda);
    assertInstanceOf(PcmDiscordPlayer.class, player);
    assertThrows(NullPointerException.class, () -> DiscordPlayer.voice(null));
  }

  @Test
  void shouldStartEmptyAndSendPcmRatherThanOpus() {
    final PcmDiscordPlayer player = this.createPlayer();
    final boolean canProvide = player.canProvide();
    final ByteBuffer frame = player.provide20MsAudio();
    final long queued = player.getQueuedMilliseconds();
    final boolean opus = player.isOpus();
    assertFalse(canProvide);
    assertNull(frame);
    assertEquals(0L, queued);
    assertFalse(opus);
  }

  @Test
  void shouldConvertLittleEndianSamplesToBigEndianFrames() {
    final PcmDiscordPlayer player = this.createPlayer();
    final ByteBuffer samples = ByteBuffer.allocate(FRAME_BYTES);
    samples.order(ByteOrder.LITTLE_ENDIAN);
    final int sampleCount = FRAME_BYTES / Short.BYTES;
    for (int index = 0; index < sampleCount; index++) {
      samples.putShort((short) index);
    }
    samples.flip();

    final boolean changed = player.applyFilter(samples, this.metadata);
    final int inputPosition = samples.position();
    final ByteBuffer frame = player.provide20MsAudio();
    assertFalse(changed, "the filter only reads the sample, so it reports no change");
    assertEquals(0, inputPosition);
    assertNotNull(frame);
    final int frameSize = frame.remaining();
    assertEquals(FRAME_BYTES, frameSize);
    frame.order(ByteOrder.BIG_ENDIAN);
    for (int index = 0; index < sampleCount; index++) {
      final short sample = frame.getShort();
      assertEquals((short) index, sample);
    }
  }

  @Test
  void shouldTreatSamplesAsLittleEndianWhateverOrderTheBufferReports() {
    final PcmDiscordPlayer player = this.createPlayer();
    final ByteBuffer samples = ByteBuffer.allocate(FRAME_BYTES);
    samples.order(ByteOrder.LITTLE_ENDIAN);
    final int sampleCount = FRAME_BYTES / Short.BYTES;
    for (int index = 0; index < sampleCount; index++) {
      samples.putShort((short) (index * 3 - 1000));
    }
    samples.flip();
    samples.order(ByteOrder.BIG_ENDIAN);

    player.applyFilter(samples, this.metadata);
    final ByteOrder orderAfter = samples.order();
    final int positionAfter = samples.position();
    final ByteBuffer frame = player.provide20MsAudio();
    assertEquals(ByteOrder.BIG_ENDIAN, orderAfter, "the order of the shared buffer is not changed");
    assertEquals(0, positionAfter);
    assertNotNull(frame);
    frame.order(ByteOrder.BIG_ENDIAN);
    for (int index = 0; index < sampleCount; index++) {
      final short sample = frame.getShort();
      assertEquals((short) (index * 3 - 1000), sample);
    }
  }

  @Test
  void shouldStripSurroundingWhitespaceFromTheTitle() {
    final PcmDiscordPlayer player = this.createPlayer();
    player.setPlaying("  \t Song \n");
    final String name = this.shownActivityName();
    assertEquals("Song", name);
  }

  @Test
  void shouldShowUnknownTitleForBlankTitles() {
    final PcmDiscordPlayer player = this.createPlayer();
    final List<String> blanks = List.of("", "   ", "\n\t");
    for (final String blank : blanks) {
      player.setPlaying(blank);
      final String name = this.shownActivityName();
      assertEquals(UNKNOWN_TITLE, name);
    }
  }

  @Test
  void shouldKeepTitlesOfExactlyTheMaximumLength() {
    final PcmDiscordPlayer player = this.createPlayer();
    final String title = "a".repeat(MAXIMUM_NAME_LENGTH);
    player.setPlaying("  " + title + "  ");
    final String name = this.shownActivityName();
    final int maximumLength = Activity.MAX_ACTIVITY_NAME_LENGTH;
    assertEquals(title, name);
    assertEquals(MAXIMUM_NAME_LENGTH, maximumLength);
  }

  @Test
  void shouldCutTitlesLongerThanTheMaximumLengthAndEndThemWithAnEllipsis() {
    final PcmDiscordPlayer player = this.createPlayer();
    final StringBuilder builder = new StringBuilder();
    for (int index = 0; index < 300; index++) {
      builder.append((char) ('a' + (index % 26)));
    }
    final String title = builder.toString();
    final String kept = title.substring(0, 127);
    final String expected = kept + ELLIPSIS;

    player.setPlaying(title);
    final String name = this.shownActivityName();
    final int length = name.length();
    assertEquals(expected, name);
    assertEquals(MAXIMUM_NAME_LENGTH, length);

    final UrlParseDump dump = UrlParseDump.of(title);
    player.setCurrentMedia(dump);
    final String dumpName = this.shownActivityName();
    assertEquals(expected, dumpName);
  }

  @Test
  void shouldCutLongTitlesWithoutSplittingAnEmoji() {
    final PcmDiscordPlayer player = this.createPlayer();
    final String emoji = "😀";
    final String tail = "b".repeat(10);
    final String splitPrefix = "a".repeat(126);
    player.setPlaying(splitPrefix + emoji + tail);
    final String withoutEmoji = this.shownActivityName();
    assertEquals(splitPrefix + ELLIPSIS, withoutEmoji, "half an emoji is dropped instead of shown as a broken character");

    final String fittingPrefix = "a".repeat(125);
    player.setPlaying(fittingPrefix + emoji + tail);
    final String withEmoji = this.shownActivityName();
    final int length = withEmoji.length();
    assertEquals(fittingPrefix + emoji + ELLIPSIS, withEmoji, "an emoji that fits is kept whole");
    assertEquals(MAXIMUM_NAME_LENGTH, length);
  }

  @Test
  void shouldCollectPartialInputUntilAFrameIsComplete() {
    final PcmDiscordPlayer player = this.createPlayer();
    final ByteBuffer first = filledBuffer(1000, (byte) 1);
    player.applyFilter(first, this.metadata);
    final boolean providesAfterFirst = player.canProvide();
    final long queuedAfterFirst = player.getQueuedMilliseconds();
    final ByteBuffer second = filledBuffer(FRAME_BYTES - 1000 + 100, (byte) 2);
    player.applyFilter(second, this.metadata);
    final boolean providesAfterSecond = player.canProvide();
    final long queuedAfterSecond = player.getQueuedMilliseconds();
    assertFalse(providesAfterFirst);
    assertEquals(0L, queuedAfterFirst);
    assertTrue(providesAfterSecond);
    assertEquals(20L, queuedAfterSecond);

    final ByteBuffer firstFrame = player.provide20MsAudio();
    assertFrameHolds(firstFrame, (byte) 1, 1000, (byte) 2);
    final ByteBuffer third = filledBuffer(FRAME_BYTES - 100, (byte) 3);
    player.applyFilter(third, this.metadata);
    final ByteBuffer secondFrame = player.provide20MsAudio();
    assertFrameHolds(secondFrame, (byte) 2, 100, (byte) 3);
    final boolean drained = player.canProvide();
    assertFalse(drained);
  }

  @Test
  void shouldSplitLargeInputIntoSeveralFrames() {
    final PcmDiscordPlayer player = this.createPlayer();
    final ByteBuffer samples = filledBuffer(FRAME_BYTES * 3 + 10, (byte) 7);
    player.applyFilter(samples, this.metadata);
    final long queued = player.getQueuedMilliseconds();
    assertEquals(60L, queued);
    for (int index = 0; index < 3; index++) {
      final ByteBuffer frame = player.provide20MsAudio();
      assertNotNull(frame);
      final int size = frame.remaining();
      assertEquals(FRAME_BYTES, size);
    }
    final ByteBuffer none = player.provide20MsAudio();
    assertNull(none);
  }

  @Test
  void shouldDropTheOldestFramesWhenThreeSecondsAreQueued() {
    final PcmDiscordPlayer player = this.createPlayer();
    for (int index = 0; index < MAXIMUM_FRAMES + 2; index++) {
      final ByteBuffer frame = filledBuffer(FRAME_BYTES, (byte) index);
      player.applyFilter(frame, this.metadata);
    }
    final long queued = player.getQueuedMilliseconds();
    assertEquals(3000L, queued);

    final ByteBuffer oldest = player.provide20MsAudio();
    assertNotNull(oldest);
    final byte first = oldest.get(0);
    assertEquals((byte) 2, first);
    final long afterPoll = player.getQueuedMilliseconds();
    assertEquals(2980L, afterPoll);
  }

  @Test
  void shouldDropQueuedFramesAndThePartialFrameOnFlush() {
    final PcmDiscordPlayer player = this.createPlayer();
    final ByteBuffer frames = filledBuffer(FRAME_BYTES * 2 + 500, (byte) 5);
    player.applyFilter(frames, this.metadata);
    player.flush();
    final boolean canProvide = player.canProvide();
    final long queued = player.getQueuedMilliseconds();
    assertFalse(canProvide);
    assertEquals(0L, queued);

    final ByteBuffer next = filledBuffer(FRAME_BYTES - 500, (byte) 9);
    player.applyFilter(next, this.metadata);
    final boolean partialWasKept = player.canProvide();
    assertFalse(partialWasKept);
    final ByteBuffer rest = filledBuffer(500, (byte) 9);
    player.applyFilter(rest, this.metadata);
    final ByteBuffer frame = player.provide20MsAudio();
    assertNotNull(frame);
    final byte[] bytes = contentOf(frame);
    for (final byte value : bytes) {
      assertEquals((byte) 9, value);
    }
  }

  @Test
  void shouldAcceptEmptyInput() {
    final PcmDiscordPlayer player = this.createPlayer();
    final ByteBuffer empty = ByteBuffer.allocate(0);
    final boolean changed = player.applyFilter(empty, this.metadata);
    final boolean canProvide = player.canProvide();
    assertFalse(changed, "the filter only reads the sample, so it reports no change");
    assertFalse(canProvide);
  }

  @Test
  void shouldRejectNullSamplesAndMetadata() {
    final PcmDiscordPlayer player = this.createPlayer();
    final ByteBuffer samples = ByteBuffer.allocate(FRAME_BYTES);
    assertThrows(NullPointerException.class, () -> player.applyFilter(null, this.metadata));
    assertThrows(NullPointerException.class, () -> player.applyFilter(samples, null));
    final boolean canProvide = player.canProvide();
    assertFalse(canProvide, "rejected samples are not queued");
  }

  /**
   * Lets several threads apply frames at the same time, each frame filled with the index of its thread.
   */
  private void produceConcurrently(final PcmDiscordPlayer player) throws Exception {
    final CountDownLatch start = new CountDownLatch(1);
    try (final ExecutorService executor = Executors.newFixedThreadPool(PRODUCERS)) {
      final List<Future<?>> futures = new ArrayList<>();
      for (int producer = 0; producer < PRODUCERS; producer++) {
        final byte marker = (byte) producer;
        final Future<?> future = executor.submit(() -> {
          start.await();
          for (int index = 0; index < FRAMES_PER_PRODUCER; index++) {
            final ByteBuffer frame = filledBuffer(FRAME_BYTES, marker);
            player.applyFilter(frame, this.metadata);
          }
          return null;
        });
        futures.add(future);
      }
      start.countDown();
      for (final Future<?> future : futures) {
        future.get(PRODUCER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
      }
    }
  }

  private static int[] countFramesPerProducer(final PcmDiscordPlayer player) {
    final int[] counts = new int[PRODUCERS];
    ByteBuffer frame = player.provide20MsAudio();
    while (frame != null) {
      final byte[] bytes = contentOf(frame);
      final byte marker = bytes[0];
      for (final byte value : bytes) {
        assertEquals(marker, value, "frames of different producers must not interleave");
      }
      counts[marker]++;
      frame = player.provide20MsAudio();
    }
    return counts;
  }

  @Test
  void shouldQueueEveryFrameFromConcurrentProducers() throws Exception {
    final PcmDiscordPlayer player = this.createPlayer();
    this.produceConcurrently(player);
    final long queued = player.getQueuedMilliseconds();
    assertEquals((long) PRODUCERS * FRAMES_PER_PRODUCER * FRAME_MILLISECONDS, queued);

    final int[] counts = countFramesPerProducer(player);
    for (final int count : counts) {
      assertEquals(FRAMES_PER_PRODUCER, count);
    }
  }

  @Test
  void shouldShowTheTitleAsTheActivityOfTheBot() {
    final PcmDiscordPlayer player = this.createPlayer();
    player.setPlaying("Never Gonna Give You Up");
    final ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
    final InOrder order = Mockito.inOrder(this.presence);
    final Presence statusVerified = order.verify(this.presence);
    statusVerified.setStatus(OnlineStatus.ONLINE);
    final Presence activityVerified = order.verify(this.presence);
    final Activity captured = captor.capture();
    activityVerified.setActivity(captured);

    final Activity activity = captor.getValue();
    final String name = activity.getName();
    final Activity.ActivityType type = activity.getType();
    assertEquals("Never Gonna Give You Up", name);
    assertEquals(Activity.ActivityType.PLAYING, type);
    assertThrows(NullPointerException.class, () -> player.setPlaying(null));
  }

  @Test
  void shouldShowTheTitleOfTheDump() {
    final PcmDiscordPlayer player = this.createPlayer();
    final UrlParseDump dump = UrlParseDump.of("Some Video");
    player.setCurrentMedia(dump);
    final VerificationMode once = Mockito.times(1);
    final List<Activity> activities = this.shownActivities(once);
    final Activity activity = activities.getFirst();
    final String name = activity.getName();
    assertEquals("Some Video", name);
  }

  @Test
  void shouldShowUnknownTitleForMissingOrBlankTitlesOfTheDump() {
    final PcmDiscordPlayer player = this.createPlayer();
    final UrlParseDump missing = GsonProvider.getSimple().fromJson("{}", UrlParseDump.class);
    final UrlParseDump blank = UrlParseDump.of("   ");
    player.setCurrentMedia(missing);
    player.setCurrentMedia(blank);
    final VerificationMode twice = Mockito.times(2);
    final List<Activity> activities = this.shownActivities(twice);
    for (final Activity activity : activities) {
      final String name = activity.getName();
      assertEquals(UNKNOWN_TITLE, name);
    }
    assertThrows(NullPointerException.class, () -> player.setCurrentMedia(null));
  }
}
