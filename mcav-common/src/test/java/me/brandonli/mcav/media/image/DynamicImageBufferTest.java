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
package me.brandonli.mcav.media.image;

import static me.brandonli.mcav.media.ResourceAssertions.assertThrowsWhileOpening;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import me.brandonli.mcav.media.source.file.FileSource;
import me.brandonli.mcav.media.source.uri.UriSource;
import me.brandonli.mcav.testing.Images;
import me.brandonli.mcav.testing.LocalHttpServer;
import me.brandonli.mcav.testing.TestMedia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class DynamicImageBufferTest {

  private static final int ANIMATION_WIDTH = 8;
  private static final int ANIMATION_HEIGHT = 4;
  private static final int FRAME_COUNT = 3;
  private static final int SECOND_FRAME_COLOR = 0xFF00FF00;

  private static final String ANIMATION_NAME = "animation.gif";
  private static final String ANIMATION_URL_PATH = "/animation.gif";
  private static final String MISSING_NAME = "missing.gif";

  private static final int TIMESTAMPED_FRAMES = 3;
  private static final long ONE_SECOND = 1_000_000L;
  private static final long TWO_SECONDS = 2_000_000L;

  private static final float DEFAULT_FRAME_RATE = 10.0f;

  private static final byte[] ANIMATION = Images.animatedGif(ANIMATION_WIDTH, ANIMATION_HEIGHT, 4, 0xFF0000, 0x00FF00, 0x0000FF);

  @TempDir
  private Path directory;

  private static int firstPixel(final ImageBuffer frame) {
    final int[] pixels = frame.getPixels();
    return pixels[0];
  }

  private Path writeAnimation() throws IOException {
    final Path gif = this.directory.resolve(ANIMATION_NAME);
    Files.write(gif, ANIMATION);
    return gif;
  }

  private static void assertEveryFrameHasItsColor(final DynamicImageBuffer animation) {
    final int count = animation.getFrameCount();
    final List<ImageBuffer> frames = animation.getFrames();
    final ImageBuffer first = frames.get(0);
    final ImageBuffer second = animation.getFrame(1);
    final ImageBuffer third = frames.get(2);
    final ImageBuffer listedSecond = frames.get(1);
    final int firstColor = firstPixel(first);
    final int secondColor = firstPixel(second);
    final int thirdColor = firstPixel(third);
    assertEquals(FRAME_COUNT, count);
    assertSame(listedSecond, second);
    assertEquals(0xFFFF0000, firstColor);
    assertEquals(SECOND_FRAME_COLOR, secondColor);
    assertEquals(0xFF0000FF, thirdColor);
  }

  @Test
  void shouldDecodeEveryFrameOfANarrowAnimation() throws IOException {
    final Path gif = this.writeAnimation();
    final FileSource source = FileSource.path(gif);
    try (final DynamicImageBuffer animation = DynamicImageBuffer.path(source)) {
      assertEveryFrameHasItsColor(animation);

      final List<ImageBuffer> frames = animation.getFrames();
      final ImageBuffer second = animation.getFrame(1);
      final int[] secondPixels = second.getPixels();
      final int lastPixel = secondPixels[31];
      final float frameRate = animation.getFrameRate();
      final int width = second.getWidth();
      final int height = second.getHeight();
      assertEquals(SECOND_FRAME_COLOR, lastPixel, "rows are not shifted by the row padding of the decoder");
      assertEquals(25.0f, frameRate, 0.01f, "every frame is shown for four hundredths of a second");
      assertEquals(ANIMATION_WIDTH, width);
      assertEquals(ANIMATION_HEIGHT, height);
      assertThrows(UnsupportedOperationException.class, () -> frames.add(second));
      assertThrows(IndexOutOfBoundsException.class, () -> animation.getFrame(FRAME_COUNT));
      assertThrows(IndexOutOfBoundsException.class, () -> animation.getFrame(-1));
    }
  }

  @Test
  void shouldReleaseItsFramesWhenClosed() throws IOException {
    final Path gif = this.writeAnimation();
    final FileSource source = FileSource.path(gif);
    final DynamicImageBuffer animation = DynamicImageBuffer.path(source);
    final ImageBuffer frame = animation.getFrame(0);
    animation.close();
    assertThrows(IllegalStateException.class, frame::getWidth);
  }

  @Test
  void shouldDownloadAnimationsFromUris() throws IOException {
    final String previousHome = System.getProperty("user.home");
    final String temporaryHome = this.directory.toString();
    System.setProperty("user.home", temporaryHome);
    try (final LocalHttpServer server = LocalHttpServer.start()) {
      server.respond(ANIMATION_URL_PATH, 200, ANIMATION);
      final URI uri = server.uri(ANIMATION_URL_PATH);
      final UriSource source = UriSource.uri(uri);
      try (final DynamicImageBuffer animation = DynamicImageBuffer.uri(source)) {
        final int count = animation.getFrameCount();
        assertEquals(FRAME_COUNT, count);
      }
    } finally {
      System.setProperty("user.home", previousHome);
    }
  }

  @Test
  void shouldRejectFilesWithoutImages() {
    final Path audio = TestMedia.audio(0.5);
    final FileSource audioSource = FileSource.path(audio);
    final Path missing = this.directory.resolve(MISSING_NAME);
    final FileSource missingSource = FileSource.path(missing);
    assertThrowsWhileOpening(IllegalArgumentException.class, () -> DynamicImageBuffer.path(audioSource));
    assertThrowsWhileOpening(ImageDecodingException.class, () -> DynamicImageBuffer.path(missingSource));
    assertThrowsWhileOpening(NullPointerException.class, () -> DynamicImageBuffer.path(null));
    assertThrowsWhileOpening(NullPointerException.class, () -> DynamicImageBuffer.uri(null));
  }

  @Test
  void shouldPreferTheReportedFrameRate() {
    final float reported = FfmpegDynamicImageBuffer.resolveFrameRate(25.0, TIMESTAMPED_FRAMES, 0L, ONE_SECOND);
    assertEquals(25.0f, reported);
  }

  @Test
  void shouldDeriveTheFrameRateFromTimestampsWhenNoneIsReported() {
    final float zero = FfmpegDynamicImageBuffer.resolveFrameRate(0.0, TIMESTAMPED_FRAMES, 0L, TWO_SECONDS);
    final float notANumber = FfmpegDynamicImageBuffer.resolveFrameRate(Double.NaN, 5, 0L, ONE_SECOND);
    final float infinite = FfmpegDynamicImageBuffer.resolveFrameRate(Double.POSITIVE_INFINITY, TIMESTAMPED_FRAMES, 0L, ONE_SECOND);
    assertEquals(1.0f, zero);
    assertEquals(4.0f, notANumber);
    assertEquals(2.0f, infinite);
  }

  @Test
  void shouldDeriveTheFrameRateFromTheTimeBetweenTheFirstAndTheLastFrame() {
    final float lateStart = FfmpegDynamicImageBuffer.resolveFrameRate(0.0, TIMESTAMPED_FRAMES, ONE_SECOND, TWO_SECONDS);
    final float sameTimestamps = FfmpegDynamicImageBuffer.resolveFrameRate(0.0, TIMESTAMPED_FRAMES, ONE_SECOND, ONE_SECOND);
    assertEquals(2.0f, lateStart, "two frame periods fit between one and two seconds, however late the first frame is");
    assertEquals(DEFAULT_FRAME_RATE, sameTimestamps);
  }

  @Test
  void shouldFallBackToTheDefaultFrameRate() {
    final float singleFrame = FfmpegDynamicImageBuffer.resolveFrameRate(-1.0, 1, 0L, ONE_SECOND);
    final float noTimestamps = FfmpegDynamicImageBuffer.resolveFrameRate(0.0, TIMESTAMPED_FRAMES, 0L, 0L);
    assertEquals(DEFAULT_FRAME_RATE, singleFrame);
    assertEquals(DEFAULT_FRAME_RATE, noTimestamps);

    final boolean positive = singleFrame > 0;
    assertTrue(positive);
  }
}
