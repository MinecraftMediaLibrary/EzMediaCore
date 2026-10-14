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

import java.nio.Buffer;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

/**
 * A frame grabber that plays a prepared script, so playback sessions can be tested without decoding real media.
 *
 * <p>The script holds frames, which are returned in order, {@link FrameGrabber.Exception checked grabber exceptions}
 * and runtime exceptions, which are thrown when reached, and {@link Delay delays}, which block the decoding thread
 * without reacting to interrupts. The grabber reports the end of the media once the script is used up. The media has
 * no known length, like a live stream, unless one is set.
 */
final class ScriptedFrameGrabber extends FrameGrabber {

  private static final int FRAME_WIDTH = 4;
  private static final int FRAME_HEIGHT = 2;

  private static final int SAMPLE_RATE = 48_000;
  private static final int AUDIO_CHANNELS = 2;

  private final Deque<Object> script;
  private final AtomicBoolean closed;
  private final AtomicLong requestedTimestamp;
  private final boolean failOnStop;
  private boolean failOnStart;
  private boolean failOnSeek;
  private long length;

  ScriptedFrameGrabber(final int width, final int height, final boolean failOnStop, final List<?> steps) {
    this.script = new ArrayDeque<>(steps);
    this.closed = new AtomicBoolean();
    this.requestedTimestamp = new AtomicLong(-1L);
    this.failOnStop = failOnStop;
    this.imageWidth = width;
    this.imageHeight = height;
    this.sampleRate = SAMPLE_RATE;
    this.audioChannels = AUDIO_CHANNELS;
  }

  static ScriptedFrameGrabber of(final Object... steps) {
    final List<Object> script = List.of(steps);
    return new ScriptedFrameGrabber(FRAME_WIDTH, FRAME_HEIGHT, false, script);
  }

  static ScriptedFrameGrabber failingToStart() {
    final ScriptedFrameGrabber grabber = of();
    grabber.failOnStart = true;
    return grabber;
  }

  static ScriptedFrameGrabber failingToStartAndStop() {
    final List<Object> noSteps = List.of();
    final ScriptedFrameGrabber grabber = new ScriptedFrameGrabber(FRAME_WIDTH, FRAME_HEIGHT, true, noSteps);
    grabber.failOnStart = true;
    return grabber;
  }

  static Frame video(final long timestampMicroseconds) {
    final Frame frame = new Frame(FRAME_WIDTH, FRAME_HEIGHT, Frame.DEPTH_UBYTE, 3);
    frame.timestamp = timestampMicroseconds;
    return frame;
  }

  static Frame audio(final long timestampMicroseconds) {
    final Frame frame = new Frame();
    frame.samples = new Buffer[] { ShortBuffer.allocate(960) };
    frame.sampleRate = SAMPLE_RATE;
    frame.audioChannels = AUDIO_CHANNELS;
    frame.timestamp = timestampMicroseconds;
    return frame;
  }

  /**
   * Returns the number of steps of the script that have not been reached yet.
   *
   * @return the remaining steps
   */
  int getRemainingSteps() {
    synchronized (this.script) {
      return this.script.size();
    }
  }

  /**
   * Makes every seek fail, like a source that cannot seek at all.
   */
  void failOnSeek() {
    this.failOnSeek = true;
  }

  @Override
  public void start() throws FrameGrabber.Exception {
    if (this.failOnStart) {
      throw new FrameGrabber.Exception("start failed on purpose");
    }
  }

  @Override
  public void stop() throws FrameGrabber.Exception {
    if (this.failOnStop) {
      throw new FrameGrabber.Exception("stop failed on purpose");
    }
  }

  @Override
  public void trigger() {}

  /**
   * Plays the script up to its next frame.
   *
   * @return the next frame, or {@code null} once the script is used up
   * @throws FrameGrabber.Exception if the script reached a checked grabber exception
   */
  @Override
  public Frame grab() throws FrameGrabber.Exception {
    Object next = this.takeNextStep();
    while (next instanceof final Delay delay) {
      delay.block();
      next = this.takeNextStep();
    }

    return this.playStep(next);
  }

  private Object takeNextStep() {
    synchronized (this.script) {
      return this.script.poll();
    }
  }

  private Frame playStep(final Object next) throws FrameGrabber.Exception {
    return switch (next) {
      case null -> null;
      case final Frame frame -> frame;
      case final FrameGrabber.Exception exception -> throw exception;
      case final RuntimeException exception -> throw exception;
      default -> {
        final String message = "Script step %s is unknown!".formatted(next);
        throw new IllegalStateException(message);
      }
    };
  }

  @Override
  public void release() {
    this.closed.set(true);
  }

  @Override
  public void setTimestamp(final long timestamp) throws FrameGrabber.Exception {
    this.requestedTimestamp.set(timestamp);

    if (this.failOnSeek) {
      throw new FrameGrabber.Exception("seek failed on purpose");
    }
  }

  boolean isClosed() {
    return this.closed.get();
  }

  long getRequestedTimestamp() {
    return this.requestedTimestamp.get();
  }

  /**
   * Gives the media a known length, which makes it seekable.
   *
   * @param lengthMicroseconds the length in microseconds
   */
  void setLength(final long lengthMicroseconds) {
    this.length = lengthMicroseconds;
  }

  @Override
  public long getLengthInTime() {
    return this.length;
  }

  /**
   * Blocks the grabbing thread for a while and ignores interrupts meanwhile, like a decoder stuck in native code.
   */
  static final class Delay {

    private static final long NANOSECONDS_PER_MILLISECOND = 1_000_000L;

    private final long milliseconds;

    Delay(final long milliseconds) {
      this.milliseconds = milliseconds;
    }

    /**
     * Blocks for the whole delay. An interrupt wakes the thread up early, so it is cleared, the thread goes back to
     * sleep for the rest of the delay, and the interrupt is restored at the end.
     */
    void block() {
      final long delayNanoseconds = this.milliseconds * NANOSECONDS_PER_MILLISECOND;
      final long end = System.nanoTime() + delayNanoseconds;
      boolean interrupted = false;
      long remaining = delayNanoseconds;
      while (remaining > 0) {
        LockSupport.parkNanos(remaining);
        interrupted |= Thread.interrupted();
        remaining = end - System.nanoTime();
      }

      if (interrupted) {
        final Thread currentThread = Thread.currentThread();
        currentThread.interrupt();
      }
    }
  }
}
