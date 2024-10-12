/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.player.buffered;

import io.github.pulsebeat02.ezmediacore.callback.VideoCallback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioSource;
import rewrite.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.MediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.input.InputParser;
import io.github.pulsebeat02.ezmediacore.utility.structure.Triple;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.Contract;



public abstract class BufferedMediaPlayer extends MediaPlayer implements BufferedPlayer {

  private final ArrayBlockingQueue<Triple<int[], byte[], Long>> frames;
  private final BufferConfiguration buffer;
  private final AtomicBoolean status;
  private final Runnable frameDisplayer;
  private final Runnable frameWatchdog;

  private long start;
  private CompletableFuture<Void> display;
  private CompletableFuture<Void> watchdog;

  BufferedMediaPlayer(
       final VideoCallback video,
       final AudioSource audio,
       final Viewers viewers,
       final Dimension pixelDimension,
       final BufferConfiguration buffer,
       final InputParser parser) {
    super(video, audio, viewers, pixelDimension, parser);
    this.buffer = buffer;
    this.status = new AtomicBoolean(false);
    this.frames = new ArrayBlockingQueue<>(this.calculateCapacity());
    this.frameDisplayer = this.getDisplayRunnable();
    this.frameWatchdog = this.getSkipRunnable();
  }

  private int calculateCapacity() {
    return this.buffer.getBuffer() * 60;
  }

  @Override
  public  BufferConfiguration getBufferConfiguration() {
    return this.buffer;
  }

  public long getElapsedMilliseconds() {
    return System.currentTimeMillis() - this.start;
  }

  @Override
  public boolean addFrame(
      final int  [] data, final byte  [] audio, final long timestamp) {
    while (this.frames.remainingCapacity() <= 1) {
      Try.sleep(TimeUnit.MILLISECONDS, 5);
    }
    return this.frames.add(Triple.ofTriple(data, audio, timestamp));
  }

  @SuppressWarnings("LoopConditionNotUpdatedInsideLoop")
  @Override
  public void bufferFrames() {
    final int target = this.calculateCapacity() >> 1;
    // noinspection StatementWithEmptyBody
    while (this.frames.size() >= target) {}
  }

  @Override
  public <T> void cancelFuture( final CompletableFuture<T> future) {
    if (future != null && !future.isDone()) {
      future.cancel(true);
    }
  }

  @Override
  public void startDisplayRunnable() {
    this.cancelFuture(this.display);
    this.display = CompletableFuture.runAsync(this.frameDisplayer);
  }

  @Override
  public void startWatchdogRunnable() {
    this.cancelFuture(this.watchdog);
    this.watchdog = CompletableFuture.runAsync(this.frameWatchdog);
  }

  @Override
  public void forceStop() {
    this.status.set(false);
  }

  @Contract(pure = true)
  private  Runnable getDisplayRunnable() {
    return () -> {
      final VideoCallback videoCallback = this.getVideoCallback();
      final AudioSource audioCallback = this.getAudioCallback();
      while (this.status.get()) {
        try {
          // process current frame
          final Triple<int[], byte[], Long> frame = this.frames.take();
          final int[] buffer = frame.getX();
          final byte[] audio = frame.getY();
          if (buffer != null) {
            videoCallback.process(buffer);
          }
          if (audio != null) {
            audioCallback.process(audio);
          }
        } catch (final InterruptedException ignored) {
        }
      }
    };
  }

  @Contract(pure = true)
  private  Runnable getSkipRunnable() {
    return () -> {
      while (this.status.get()) {
        // skip frames if necessary
        // For example, if the passed time is 40 ms, and the time of the
        // current frame is 30 ms, we have to skip. If it is equal, we are
        // bound to get behind, so skip
        try {
          final long passed = Instant.now().toEpochMilli() - this.start;
          Triple<int[], byte[], Long> skip = this.frames.take();
          while (skip.getZ() <= passed) {
            this.frames.take();
            skip = this.frames.take();
          }
        } catch (final InterruptedException ignored) {
        }
      }
    };
  }

  @Override
  public long getStart() {
    return this.start;
  }

  @Override
  public void setStart(final long start) {
    this.start = start;
  }

  @Override
  public boolean isExecuting() {
    return this.status.get();
  }
}
