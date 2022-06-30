package io.github.pulsebeat02.ezmediacore.player.buffered;

import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.FrameConfiguration;
import io.github.pulsebeat02.ezmediacore.player.MediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.input.InputParser;
import io.github.pulsebeat02.ezmediacore.utility.misc.Try;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Triple;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class BufferedMediaPlayer extends MediaPlayer implements BufferedPlayer {

  private final ArrayBlockingQueue<Triple<int[], byte[], Long>> frames;
  private final BufferConfiguration buffer;
  private final AtomicBoolean status;
  private final Runnable frameDisplayer;
  private final Runnable frameWatchdog;

  private long start;
  private CompletableFuture<Void> display;
  private CompletableFuture<Void> watchdog;
  private Consumer<int[]> videoCallback;
  private Consumer<byte[]> audioCallback;

  private String format;
  private int blockSize;
  private int rate;
  private int channels;

  BufferedMediaPlayer(
      @NotNull final Callback callback,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension,
      @NotNull final BufferConfiguration buffer,
      @NotNull final FrameConfiguration fps,
      @Nullable final SoundKey key,
      @NotNull final InputParser parser) {
    super(callback, viewers, pixelDimension, fps, key, parser);
    this.buffer = buffer;
    this.status = new AtomicBoolean(false);
    this.frames = new ArrayBlockingQueue<>(this.calculateCapacity());
    this.frameDisplayer = this.getDisplayRunnable();
    this.frameWatchdog = this.getSkipRunnable();
  }

  private int calculateCapacity() {
    return this.buffer.getBuffer() * this.getFrameConfiguration().getFps();
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
  public boolean addFrame(
      final int @Nullable [] data, final byte @Nullable [] audio, final long timestamp) {
    while (this.frames.remainingCapacity() <= 1) {
      Try.sleep(TimeUnit.MILLISECONDS, 5);
    }
    return this.frames.add(Triple.ofTriple(data, audio, timestamp));
  }

  @SuppressWarnings("LoopConditionNotUpdatedInsideLoop")
  @Override
  public void bufferFrames() {
    final int target = (this.buffer.getBuffer() * this.getFrameConfiguration().getFps()) >> 1;
    // noinspection StatementWithEmptyBody
    while (this.frames.size() >= target) {}
    this.playAudio();
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
    this.status.set(false);
  }

  @Contract(pure = true)
  private @NotNull Runnable getDisplayRunnable() {
    return () -> {
      final Callback callback = this.getCallback();
      while (this.status.get()) {
        try {
          // process current frame
          final Triple<int[], byte[], Long> frame = this.frames.take();
          final int[] buffer = frame.getX();
          final byte[] audio = frame.getY();
          if (buffer != null) {
            callback.process(buffer);
            this.videoCallback.accept(buffer);
          }
          if (audio != null) {
            this.audioCallback.accept(audio);
          }
        } catch (final InterruptedException ignored) {
        }
      }
    };
  }

  @Contract(pure = true)
  private @NotNull Runnable getSkipRunnable() {
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
  public void setCustomVideoAdapter(@NotNull final Consumer<int[]> pixels) {
    this.videoCallback = pixels;
  }

  @Override
  public void setCustomAudioAdapter(
      @NotNull final Consumer<byte[]> audio,
      @NotNull final String format,
      final int blockSize,
      final int rate,
      final int channels) {
    this.audioCallback = audio;
    this.format = format;
    this.blockSize = blockSize;
    this.rate = rate;
    this.channels = channels;
  }

  @Override
  public boolean isBuffered() {
    return true;
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

  @Override
  public @NotNull String getAudioFormat() {
    return this.format;
  }

  @Override
  public int getAudioBlockSize() {
    return this.blockSize;
  }

  @Override
  public int getAudioBitrate() {
    return this.rate;
  }

  @Override
  public int getAudioChannels() {
    return this.channels;
  }
}
