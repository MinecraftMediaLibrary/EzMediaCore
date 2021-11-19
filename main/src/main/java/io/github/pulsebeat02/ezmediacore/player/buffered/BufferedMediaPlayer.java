package io.github.pulsebeat02.ezmediacore.player.buffered;

import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.FrameConfiguration;
import io.github.pulsebeat02.ezmediacore.player.MediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BufferedMediaPlayer extends MediaPlayer implements BufferedPlayer {

  private final ArrayBlockingQueue<Pair<int[], Long>> frames;
  private final BufferConfiguration buffer;
  private final MutableBoolean status;
  private final Runnable frameDisplayer;
  private final Runnable frameWatchdog;

  private long start;
  private CompletableFuture<Void> display;
  private CompletableFuture<Void> watchdog;

  BufferedMediaPlayer(
      @NotNull final Callback callback,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension,
      @NotNull final BufferConfiguration buffer,
      @NotNull final FrameConfiguration fps,
      @Nullable final SoundKey key) {
    super(callback, viewers, pixelDimension, fps, key);
    this.buffer = buffer;
    this.status = new MutableBoolean(false);
    this.frames =
        new ArrayBlockingQueue<>(this.buffer.getBuffer() * this.getFrameConfiguration().getFps());
    this.frameDisplayer = this.getDisplayRunnable();
    this.frameWatchdog = this.getSkipRunnable();
  }

  @Override
  public @NotNull BufferConfiguration getBufferConfiguration() {
    return this.buffer;
  }

  @Override
  public long getElapsedMilliseconds() {
    return System.currentTimeMillis() - this.start;
  }

  @Override
  public boolean addFrame(final int @NotNull [] data, final long timestamp) {
    while (this.frames.remainingCapacity() <= 1) {
      try {
        TimeUnit.MILLISECONDS.sleep(5);
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }
    return this.frames.add(Pair.ofPair(data, timestamp));
  }

  @Override
  public void bufferFrames() {
    final int target = (this.buffer.getBuffer() * this.getFrameConfiguration().getFps()) >> 1;
    while (this.frames.size() >= target) {}
  }

  @Override
  public <T> void cancelFuture(@Nullable final CompletableFuture<T> future) {
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
    this.status.setValue(false);
  }

  @Contract(pure = true)
  private @NotNull Runnable getDisplayRunnable() {
    return () -> {
      final Callback callback = this.getCallback();
      while (this.status.booleanValue()) {
        try {
          // process current frame
          callback.process(this.frames.take().getKey());
        } catch (final InterruptedException ignored) {
        }
      }
    };
  }

  @Contract(pure = true)
  private @NotNull Runnable getSkipRunnable() {
    return () -> {
      while (this.status.booleanValue()) {
        // skip frames if necessary
        // For example, if the passed time is 40 ms, and the time of the
        // current frame is 30 ms, we have to skip. If it is equal, we are
        // bound to get behind, so skip
        try {
          final long passed = Instant.now().toEpochMilli() - this.start;
          Pair<int[], Long> skip = this.frames.take();
          while (skip.getValue() <= passed) {
            this.frames.take();
            skip = this.frames.take();
          }
        } catch (final InterruptedException ignored) {
        }
      }
    };
  }

  @Override
  public boolean isBuffered() {
    return true;
  }

  @Override
  public void setStart(final long start) {
    this.start = start;
  }

  @Override
  public long getStart() {
    return this.start;
  }

  @Override
  public boolean isExecuting() {
    return this.status.booleanValue();
  }
}
