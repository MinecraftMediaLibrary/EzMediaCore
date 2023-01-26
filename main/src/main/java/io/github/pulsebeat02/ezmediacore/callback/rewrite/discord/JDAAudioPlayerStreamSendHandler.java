package io.github.pulsebeat02.ezmediacore.callback.rewrite.discord;

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
