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
package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.callback.audio.JDAAudioStream;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class JDAAudioPlayerStreamSendHandler implements JDAAudioStream {
  private static final int OPUS_FRAME_SIZE;
  private static final ByteBuffer EMPTY_BUFFER;

  static {
    OPUS_FRAME_SIZE = 960;
    EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);
  }

  private final int audioBufferSize;
  private final AudioInputStream audioSource;

  private boolean paused;

  JDAAudioPlayerStreamSendHandler(@NotNull final AudioInputStream inSource) {
    final AudioFormat baseFormat = inSource.getFormat();
    final AudioFormat audioFormat = this.getConvertedAudioFormat(baseFormat);
    this.audioSource = AudioSystem.getAudioInputStream(audioFormat, inSource);
    this.audioBufferSize = OPUS_FRAME_SIZE * audioFormat.getFrameSize();
  }

  private @NotNull AudioFormat getConvertedAudioFormat(@NotNull final AudioFormat baseFormat) {

    final float samplingRate = baseFormat.getSampleRate();
    final int sampleSizeInBits = baseFormat.getSampleSizeInBits();
    final int channels = baseFormat.getChannels();
    final int frameSize = baseFormat.getFrameSize();
    final float frameRate = baseFormat.getFrameRate();

    final int finalSampleSize = sampleSizeInBits != -1 ? sampleSizeInBits : 16;
    final int finalFrameSize = frameSize != -1 ? frameSize : channels << 1;

    return new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        samplingRate,
        finalSampleSize,
        channels,
        finalFrameSize,
        frameRate,
        true);
  }

  @Override
  public boolean canProvide() {
    return !this.paused;
  }

  @Override
  public @NotNull ByteBuffer provide20MsAudio() {
    try {
      final byte[] audio = new byte[this.audioBufferSize];
      final int amountRead = this.audioSource.read(audio, 0, audio.length);
      return amountRead > 0 ? ByteBuffer.wrap(audio) : EMPTY_BUFFER;
    } catch (final IOException e) {
      e.printStackTrace();
      return EMPTY_BUFFER;
    }
  }

  @Override
  public void pause() {
    this.paused = true;
  }

  @Override
  public void play() {
    this.paused = false;
  }
}
