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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import me.brandonli.mcav.bukkit.media.map.MapLayout;
import me.brandonli.mcav.bukkit.media.mcv2.Mcv2.Pool;
import me.brandonli.mcav.bukkit.media.mcv2.Mcv2.Settings;
import me.brandonli.mcav.bukkit.media.mcv2.transport.TransportPages;
import me.brandonli.mcav.bukkit.testing.TestMedia;
import me.brandonli.mcav.bukkit.testing.UtilityClassAssertions;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Pre-encoding: a video file becomes the same stream a frame-by-frame encode gives, inside the budget, and every way
 * the video can fail to open, decode or close is reported.
 */
final class Mcv2FileEncoderTest {

  private static final int WIDTH = 64;

  private static final int HEIGHT = 48;

  @TempDir
  private Path directory;

  /** Splits a stream into its frames. */
  private static List<byte[]> frames(final byte[] stream) {
    final List<byte[]> frames = new ArrayList<>();
    final ByteBuffer buffer = ByteBuffer.wrap(stream).order(ByteOrder.LITTLE_ENDIAN);
    while (buffer.hasRemaining()) {
      final byte[] frame = new byte[buffer.getInt()];
      buffer.get(frame);
      frames.add(frame);
    }
    return frames;
  }

  @Test
  void shouldEncodeAVideoFileLikeAFrameByFrameEncode() throws Exception {
    final Path video = TestMedia.video();
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final List<Long> progress = new ArrayList<>();
    final Mcv2FileEncoder.Result result;
    try (final Pool budget = new Pool(2)) {
      result = Mcv2FileEncoder.encode(
        Mcv2FileEncoder.ffmpeg(video, WIDTH, HEIGHT),
        WIDTH,
        HEIGHT,
        Settings.DEFAULT,
        budget,
        out,
        progress::add
      );
    }
    final int count = TestMedia.VIDEO_SECONDS * TestMedia.VIDEO_FRAME_RATE;
    final long frameCount = result.frames();
    assertEquals(count, frameCount);
    final int progressCount = progress.size();
    assertEquals(count, progressCount);
    final long lastProgress = progress.getLast();
    assertEquals(count, lastProgress);
    final boolean hasSeveralKeyframes = result.keyframes() >= 2;
    assertTrue(hasSeveralKeyframes);
    final boolean tookTime = result.nanoseconds() > 0;
    assertTrue(tookTime);
    final boolean tookTimePerFrame = result.millisecondsPerFrame() > 0;
    assertTrue(tookTimePerFrame);

    final List<byte[]> frames = frames(out.toByteArray());
    final int writtenFrameCount = frames.size();
    assertEquals(count, writtenFrameCount);
    final long expectedBytes = result.bytes();
    final long writtenBytes = frames
      .stream()
      .mapToLong(frame -> frame.length)
      .sum();
    assertEquals(expectedBytes, writtenBytes);

    final Mcv2 alone = new Mcv2(Settings.DEFAULT, ForkJoinPool.commonPool(), 1, false);
    alone.setFrameLimit(TransportPages.capacity());
    try (final Mcv2FileEncoder.FrameReader reader = Mcv2FileEncoder.ffmpeg(video, WIDTH, HEIGHT)) {
      final byte[] rgb = new byte[WIDTH * HEIGHT * 3];
      for (int frameNumber = 0; frameNumber < count; frameNumber++) {
        final boolean readPicture = reader.read(rgb);
        assertTrue(readPicture);
        final byte[] expectedFrame = alone.encode(rgb, WIDTH, HEIGHT, frameNumber);
        final byte[] writtenFrame = frames.get(frameNumber);
        assertArrayEquals(expectedFrame, writtenFrame, "frame " + frameNumber);
        final long frameId = Mcv2Decoder.parse(frames.get(frameNumber)).getFrameId();
        assertEquals(frameNumber, frameId);
      }
      final boolean readPastTheEnd = reader.read(rgb);
      assertFalse(readPastTheEnd);
    }
  }

  @Test
  void shouldKeepEveryFrameSmallEnoughForTheDefaultPageSlotsOfItsWall() throws Exception {
    final int width = 8 * MapLayout.MAP_SIZE;
    final int height = 4 * MapLayout.MAP_SIZE;
    final int limit = Mcv2Configuration.MAXIMUM_PAGE_SLOTS * TransportPages.capacity();
    final byte[] checkerboard = checkerboard(width, height);
    final Mcv2 unlimited = new Mcv2(Settings.DEFAULT, ForkJoinPool.commonPool(), 1, false);
    final boolean unlimitedFrameTooLarge = unlimited.encode(checkerboard, width, height, 0).length > limit;
    assertTrue(unlimitedFrameTooLarge);

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (final Pool budget = new Pool(1)) {
      Mcv2FileEncoder.encode(repeating(checkerboard, 2), width, height, Settings.DEFAULT, budget, out, count -> {});
    }
    final List<byte[]> frames = frames(out.toByteArray());
    final int frameCount = frames.size();
    assertEquals(2, frameCount);
    for (final byte[] frame : frames) {
      final boolean withinLimit = frame.length <= limit;
      assertTrue(withinLimit, "a frame of " + frame.length + " bytes");
    }
    final int expectedLength = checkerboard.length;
    final int decodedLength = Mcv2Decoder.decode(frames.getFirst(), null, 0).length;
    assertEquals(expectedLength, decodedLength);
  }

  @Test
  void shouldReadThePicturesOfAVideo() throws Exception {
    final byte[] rgb = new byte[WIDTH * HEIGHT * Mcv2Decoder.CHANNELS];
    try (final Mcv2FileEncoder.FrameReader reader = Mcv2FileEncoder.ffmpeg(TestMedia.video(), WIDTH, HEIGHT)) {
      final boolean readPicture = reader.read(rgb);
      assertTrue(readPicture);
    }

    final boolean allBlack = Arrays.equals(new byte[rgb.length], rgb);
    assertFalse(allBlack, "the first picture of the test pattern is not all black");
  }

  @Test
  void shouldCountTheKeyframesOfTheStreamItWrites() throws Exception {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Mcv2FileEncoder.Result result;
    try (final Pool budget = new Pool(1)) {
      result = Mcv2FileEncoder.encode(repeating(checkerboard(WIDTH, HEIGHT), 3), WIDTH, HEIGHT, Settings.DEFAULT, budget, out, _ -> {});
    }
    long keyframes = 0;
    for (final byte[] frame : frames(out.toByteArray())) {
      if (Mcv2Decoder.parse(frame).isKeyframe()) {
        keyframes++;
      }
    }
    final long frameCount = result.frames();
    assertEquals(3, frameCount);
    final long countedKeyframes = result.keyframes();
    assertEquals(keyframes, countedKeyframes, "the keyframes in the stream of three frames");
  }

  @Test
  void shouldMeasureItsEncodesWithinTheTimeOfTheCall() throws Exception {
    final Mcv2FileEncoder.Result result;
    final long started = System.nanoTime();
    try (final Pool budget = new Pool(1)) {
      result = Mcv2FileEncoder.encode(
        repeating(checkerboard(WIDTH, HEIGHT), 3),
        WIDTH,
        HEIGHT,
        Settings.DEFAULT,
        budget,
        new ByteArrayOutputStream(),
        _ -> {}
      );
    }
    final long elapsed = System.nanoTime() - started;
    final boolean withinTheCall = result.nanoseconds() >= 0 && result.nanoseconds() <= elapsed;
    assertTrue(withinTheCall, result.nanoseconds() + " ns in a call of " + elapsed + " ns");
  }

  @Test
  void shouldLimitFramesToTheDefaultPageSlotsOfAWallOfTheVideosNativeSize() {
    final int capacity = TransportPages.capacity();
    final int onePixelLimit = Mcv2FileEncoder.screenFrameLimit(1, 1);
    assertEquals(capacity, onePixelLimit);
    final int oneMapLimit = Mcv2FileEncoder.screenFrameLimit(MapLayout.MAP_SIZE, MapLayout.MAP_SIZE);
    assertEquals(capacity, oneMapLimit);
    final int expectedTwoMapLimit = 2 * capacity;
    final int widerThanOneMapLimit = Mcv2FileEncoder.screenFrameLimit(MapLayout.MAP_SIZE + 1, MapLayout.MAP_SIZE);
    assertEquals(expectedTwoMapLimit, widerThanOneMapLimit);
    final int expectedTwoMapLimitForHeight = 2 * capacity;
    final int tallerThanOneMapLimit = Mcv2FileEncoder.screenFrameLimit(MapLayout.MAP_SIZE, MapLayout.MAP_SIZE + 1);
    assertEquals(expectedTwoMapLimitForHeight, tallerThanOneMapLimit);
    final int expectedSevenMapLimit = 7 * capacity;
    final int sevenMapLimit = Mcv2FileEncoder.screenFrameLimit(7 * MapLayout.MAP_SIZE, MapLayout.MAP_SIZE);
    assertEquals(expectedSevenMapLimit, sevenMapLimit);
    final int expectedFullHdLimit = Mcv2Configuration.MAXIMUM_PAGE_SLOTS * capacity;
    final int fullHdLimit = Mcv2FileEncoder.screenFrameLimit(1920, 1080);
    assertEquals(expectedFullHdLimit, fullHdLimit);
  }

  private static byte[] checkerboard(final int width, final int height) {
    final byte[] picture = new byte[width * height * Mcv2Decoder.CHANNELS];
    for (int pixel = 0; pixel < width * height; pixel++) {
      final byte shade = (byte) ((((pixel % width) + pixel / width) % 2) * 255);
      Arrays.fill(picture, pixel * Mcv2Decoder.CHANNELS, (pixel + 1) * Mcv2Decoder.CHANNELS, shade);
    }
    return picture;
  }

  private static Mcv2FileEncoder.FrameReader repeating(final byte[] picture, final int count) {
    return new Mcv2FileEncoder.FrameReader() {
      private int read;

      @Override
      public boolean read(final byte[] pictureBytes) {
        if (this.read == count) {
          return false;
        }
        System.arraycopy(picture, 0, pictureBytes, 0, picture.length);
        this.read++;
        return true;
      }

      @Override
      public void close() {}
    };
  }

  @Test
  void shouldStopWhenInterruptedAndCloseTheVideo() {
    final AtomicBoolean closed = new AtomicBoolean();
    final Mcv2FileEncoder.FrameReader interrupting = new Mcv2FileEncoder.FrameReader() {
      @Override
      public boolean read(final byte[] rgb) {
        Thread.currentThread().interrupt();
        return true;
      }

      @Override
      public void close() {
        closed.set(true);
      }
    };
    try (final Pool budget = new Pool(1)) {
      assertThrows(InterruptedException.class, () ->
        Mcv2FileEncoder.encode(interrupting, WIDTH, HEIGHT, Settings.DEFAULT, budget, new ByteArrayOutputStream(), _ -> {})
      );
    }
    final boolean stillInterrupted = Thread.interrupted();
    assertFalse(stillInterrupted);
    final boolean videoClosed = closed.get();
    assertTrue(videoClosed);

    final double emptyMillisecondsPerFrame = new Mcv2FileEncoder.Result(0, 0, 0, 0).millisecondsPerFrame();
    assertEquals(0, emptyMillisecondsPerFrame);
    final double millisecondsPerFrame = new Mcv2FileEncoder.Result(2, 0, 0, 5_000_000).millisecondsPerFrame();
    assertEquals(2.5, millisecondsPerFrame);
  }

  @Test
  void shouldRefuseWhatItCannotEncode() throws IOException {
    final Mcv2FileEncoder.FrameReader none = mock(Mcv2FileEncoder.FrameReader.class);
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (final Pool budget = new Pool(1)) {
      assertThrows(NullPointerException.class, () -> Mcv2FileEncoder.encode(null, 1, 1, Settings.DEFAULT, budget, out, _ -> {}));
      assertThrows(IllegalArgumentException.class, () -> Mcv2FileEncoder.encode(none, 0, 1, Settings.DEFAULT, budget, out, _ -> {}));
      assertThrows(IllegalArgumentException.class, () -> Mcv2FileEncoder.encode(none, 1, 0, Settings.DEFAULT, budget, out, _ -> {}));
      assertThrows(NullPointerException.class, () -> Mcv2FileEncoder.encode(none, 1, 1, null, budget, out, _ -> {}));
      assertThrows(NullPointerException.class, () -> Mcv2FileEncoder.encode(none, 1, 1, Settings.DEFAULT, null, out, _ -> {}));
      assertThrows(NullPointerException.class, () -> Mcv2FileEncoder.encode(none, 1, 1, Settings.DEFAULT, budget, null, _ -> {}));
      assertThrows(NullPointerException.class, () -> Mcv2FileEncoder.encode(none, 1, 1, Settings.DEFAULT, budget, out, null));
    }
    assertThrows(NullPointerException.class, () -> Mcv2FileEncoder.ffmpeg(null, 1, 1));
    assertThrows(IllegalArgumentException.class, () -> Mcv2FileEncoder.ffmpeg(this.directory, 0, 1));
    assertThrows(IllegalArgumentException.class, () -> Mcv2FileEncoder.ffmpeg(this.directory, 1, 0));
    final IOException missing = assertThrows(IOException.class, () -> Mcv2FileEncoder.ffmpeg(this.directory.resolve("missing.mp4"), 8, 8));
    final String message = missing.getMessage();
    final boolean reportsTheOpen = message.startsWith("Cannot open ");
    assertTrue(reportsTheOpen);
  }

  @Test
  void shouldCloseAVideoThatCannotStart() throws Exception {
    final FFmpegFrameGrabber grabber = mock(FFmpegFrameGrabber.class);
    doThrow(new FFmpegFrameGrabber.Exception("no stream!")).when(grabber).start();
    doThrow(new FFmpegFrameGrabber.Exception("not open!")).when(grabber).close();
    final IOException thrown = assertThrows(IOException.class, () -> Mcv2FileEncoder.open(grabber, "clip.mp4", 8, 8));
    final String message = thrown.getMessage();
    final boolean reportsTheStart = message.startsWith("Cannot open clip.mp4 (no stream");
    assertTrue(reportsTheStart, thrown.getMessage());

    verify(grabber).close();
  }

  @Test
  void shouldReportFramesItCannotDecodeAndAVideoItCannotClose() throws Exception {
    final FFmpegFrameGrabber grabber = mock(FFmpegFrameGrabber.class);
    when(grabber.grabImage()).thenThrow(new FFmpegFrameGrabber.Exception("broken!"));
    doThrow(new FFmpegFrameGrabber.Exception("stuck!")).when(grabber).close();
    final Mcv2FileEncoder.GrabberReader reader = new Mcv2FileEncoder.GrabberReader(grabber, 8, 8);
    final IOException decodeFailure = assertThrows(IOException.class, () -> reader.read(new byte[192]));
    final String decodeMessage = decodeFailure.getMessage();
    final boolean reportsTheDecode = decodeMessage.startsWith("Cannot decode the video (broken");
    assertTrue(reportsTheDecode);

    final IOException closeFailure = assertThrows(IOException.class, reader::close);
    final String closeMessage = closeFailure.getMessage();
    final boolean reportsTheClose = closeMessage.startsWith("Cannot close the video (stuck");
    assertTrue(reportsTheClose);
  }

  @Test
  void shouldNotBeInstantiable() throws ReflectiveOperationException {
    UtilityClassAssertions.assertNotInstantiable(Mcv2FileEncoder.class);
  }

  @Test
  void shouldPackPaddedRows() {
    final Frame frame = new Frame(2, 2, Frame.DEPTH_UBYTE, 3, 8);
    final ByteBuffer pixels = (ByteBuffer) frame.image[0];
    for (int index = 0; index < 16; index++) {
      pixels.put(index, (byte) index);
    }
    final byte[] rgb = new byte[12];
    Mcv2FileEncoder.copy(frame, rgb, 2, 2);
    final byte[] expected = new byte[] { 0, 1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 13 };
    assertArrayEquals(expected, rgb);

    assertThrows(IllegalStateException.class, () -> Mcv2FileEncoder.copy(frame, rgb, 3, 2));
    assertThrows(IllegalStateException.class, () -> Mcv2FileEncoder.copy(frame, rgb, 2, 3));
  }
}
