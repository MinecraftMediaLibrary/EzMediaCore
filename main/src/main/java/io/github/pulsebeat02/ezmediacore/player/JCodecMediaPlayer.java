/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.player;

import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.utility.Pair;
import io.github.pulsebeat02.ezmediacore.utility.RequestUtils;
import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Size;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*

Algorithm for frame consuming into a player. We can retrieve the frames,
and put them into a ArrayBlockingQueue. We store the ArrayBlockingQueue
with both int[] as the frame data, and also a double for the timestamp.
Next, we keep track of an internal time.

Timestamp for each frame is calculated with the following equation.
Suppose the function T(f) takes in a frame called f and returns the
timestamp of that specific frame.

T(f) = f.pts * f.timebase

We can calculate this asynchronously when we fetch the frame as it
can be slow doing the decimal division. We put these into our data
structure to store the next frames.

For our display thread, we can continuously fetch from our frame
data structure. We have to account for some cases which may lead
to the downfall of the program.

What if the data structure is empty?
In this rare case where FFmpeg isn't fast enough to deliver frames
compared to display frames, we are lucky our data structure is
blocking, so we just block the thread until we are able to get the
next frame.

What if we are behind in frames?
This is a bit harder, as it requires likely a faster algorithm. One
way to do this is by looping over the next frames while keeping track
of the current time (Instant.now()) and trying to display the frame
after the frame that matched up our time. Obviously, this may be a
bit slow, and that overtime for the loop also takes some time to
process too, so we must account for that as well.
 */
public final class JCodecMediaPlayer extends MediaPlayer implements BufferedPlayer {

  private ArrayBlockingQueue<Pair<int[], Long>> frames;
  private final BufferConfiguration buffer;
  private long delay;
  private long start;

  private FrameGrab grabber;
  private boolean paused;
  private volatile CompletableFuture<Void> framePlayer;
  private volatile CompletableFuture<Void> audioPlayer;
  private volatile boolean firstFrame;

  JCodecMediaPlayer(
      @NotNull final Callback callback,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension,
      @NotNull final BufferConfiguration buffer,
      @NotNull final FrameConfiguration fps,
      @Nullable final SoundKey key) {
    super(callback, viewers, pixelDimension, fps, key);
    this.buffer = buffer;
    this.modifyPlayerAttributes();
  }

  @Override
  public @NotNull BufferConfiguration getBufferConfiguration() {
    return this.buffer;
  }

  private void modifyPlayerAttributes() {
    final int fps = this.getFrameConfiguration().getFps();
    this.frames = new ArrayBlockingQueue<>(this.buffer.getBuffer() * fps);
    this.delay = 1000L / fps;
    this.firstFrame = false;
  }

  @Override
  public void release() {
    super.release();
    this.paused = false;
    if (this.grabber != null) {
      this.grabber = null;
    }
  }

  @Override
  public void resume(@NotNull final MrlConfiguration mrl, @NotNull final Object... arguments) {
    super.resume(mrl, arguments);
    this.initializePlayer(
        mrl, DelayConfiguration.ofDelay(System.currentTimeMillis() - this.start), arguments);
    this.paused = false;
    this.play();
  }

  @Override
  public void pause() {
    super.pause();
    this.stopAudio();
    this.paused = true;
    this.start = System.currentTimeMillis();
  }

  @Override
  public void start(@NotNull final MrlConfiguration mrl, @NotNull final Object... arguments) {
    super.start(mrl, arguments);
    this.setDirectVideoMrl(RequestUtils.getVideoURLs(mrl).get(0));
    this.setDirectAudioMrl(RequestUtils.getAudioURLs(mrl).get(0));
    if (this.grabber == null) {
      this.initializePlayer(mrl, DelayConfiguration.DELAY_0_MS, arguments);
    }
    this.paused = false;
    this.play();
    this.start = System.currentTimeMillis();
  }

  private void runPlayer() {
    final Dimension dimension = this.getDimensions();
    CompletableFuture.runAsync(
        () -> {
          Picture frame;
          while (!this.paused) {
            try {
              if ((frame = this.grabber.getNativeFrame()) == null) {
                break;
              }
              if (!this.firstFrame) {
                this.firstFrame = true;
                this.start = Instant.now().toEpochMilli();
              }
              final long timestamp = Instant.now().toEpochMilli() - this.start;
              while (JCodecMediaPlayer.this.frames.remainingCapacity() <= 1) {
                try {
                  TimeUnit.MILLISECONDS.sleep(5);
                } catch (final InterruptedException e) {
                  e.printStackTrace();
                }
              }
              JCodecMediaPlayer.this.frames.add(
                  Pair.ofPair(VideoFrameUtils.toResizedColorArray(frame, dimension), timestamp));
            } catch (final IOException e) {
              e.printStackTrace();
            }
          }
        },
        ExecutorProvider.ENCODER_HANDLER);
  }

  private void play() {
    this.runPlayer();
    this.delayFrames();
    this.audioPlayer = this.updateAudioPlayer();
    this.framePlayer = this.updateFramePlayer();
  }

  private void delayFrames() {
    final int target = (this.buffer.getBuffer() * this.getFrameConfiguration().getFps()) >> 1;
    while (true) {
      if (this.frames.size() == target) { // block until frame size met
        break;
      }
    }
  }

  private @NotNull CompletableFuture<Void> updateAudioPlayer() {
    if (this.audioPlayer != null && !this.audioPlayer.isDone()) {
      this.audioPlayer.cancel(true);
    }
    return CompletableFuture.runAsync(
        () -> {
          while (true) {
            if (this.firstFrame) {
              this.playAudio();
              this.firstFrame = false;
              break;
            }
          }
        },
        ExecutorProvider.AUDIO_HANDLER);
  }

  private @NotNull CompletableFuture<Void> updateFramePlayer() {
    final Callback callback = this.getCallback();
    if (this.framePlayer != null && !this.framePlayer.isDone()) {
      this.framePlayer.cancel(true);
    }
    return CompletableFuture.runAsync(
        () -> {
          while (!this.paused) {
            try {
              // process current frame
              final Pair<int[], Long> frame = this.frames.take();
              callback.process(frame.getKey());

              // wait delay
              TimeUnit.MILLISECONDS.sleep(this.delay - 5);

              // skip frames if necessary
              // For example, if the passed time is 40 ms, and the time of the
              // current frame is 30 ms, we have to skip. If it is equal, we are
              // bound to get behind, so skip
              final long passed = Instant.now().toEpochMilli() - this.start;
              Pair<int[], Long> skip = this.frames.peek();
              while (skip.getValue() <= passed) {
                skip = this.frames.poll();
              }
            } catch (final InterruptedException e) {
              e.printStackTrace();
            }
          }
        },
        ExecutorProvider.FRAME_HANDLER);
  }

  @Override
  public void initializePlayer(
      @NotNull final MrlConfiguration mrl,
      @NotNull final DelayConfiguration configuration,
      @NotNull final Object... arguments) {
    final Dimension dimension = this.getDimensions();
    try {
      this.grabber =
          FrameGrab.createFrameGrab(
              NIOUtils.readableFileChannel(this.getDirectVideoMrl().getMrl()));
      this.grabber.seekToSecondPrecise(configuration.getDelay() / 1000.0F);
      this.grabber.getMediaInfo().setDim(new Size(dimension.getWidth(), dimension.getHeight()));
    } catch (final IOException | JCodecException e) {
      e.printStackTrace();
    }
  }

  @Override
  public @NotNull PlayerType getPlayerType() {
    return PlayerType.JCODEC;
  }

  @Override
  public boolean isBuffered() {
    return true;
  }

  @Override
  public long getElapsedMilliseconds() {
    return System.currentTimeMillis() - this.start;
  }

  public static final class Builder extends VideoBuilder {

    private BufferConfiguration bufferSize = BufferConfiguration.BUFFER_15;

    Builder() {}

    @Contract("_ -> this")
    @Override
    public Builder callback(@NotNull final Callback callback) {
      super.callback(callback);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public Builder frameRate(@NotNull final FrameConfiguration rate) {
      super.frameRate(rate);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public Builder dims(@NotNull final Dimension dims) {
      super.dims(dims);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public Builder soundKey(@NotNull final SoundKey key) {
      super.soundKey(key);
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder buffer(@NotNull final BufferConfiguration bufferSize) {
      this.bufferSize = bufferSize;
      return this;
    }

    @Contract(" -> new")
    @Override
    public @NotNull MediaPlayer build() {
      super.init();
      final Callback callback = this.getCallback();
      return new JCodecMediaPlayer(
          callback,
          callback.getWatchers(),
          this.getDims(),
          this.bufferSize,
          this.getRate(),
          this.getKey());
    }
  }
}
