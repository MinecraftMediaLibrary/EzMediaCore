package io.github.pulsebeat02.ezmediacore.pipeline.output.audio;

import org.jetbrains.annotations.Nullable;
import io.github.pulsebeat02.ezmediacore.pipeline.frame.FramePacket;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class JDAFrameOutput extends DiscordSendHandlerOutput {

  private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);

  private final Queue<ByteBuffer> audioQueue;

  public JDAFrameOutput() {
    this.audioQueue = new ConcurrentLinkedQueue<>();
  }

  @Override
  public void output(final FramePacket input) {
    final byte[] audioSamples = input.getAudioSamples();
    if (audioSamples != null) {
      this.audioQueue.add(ByteBuffer.wrap(audioSamples));
    }
  }

  @Override
  public boolean canProvide() {
    return !this.audioQueue.isEmpty();
  }

  @Nullable
  @Override
  public ByteBuffer provide20MsAudio() {
    return this.canProvide() ? this.audioQueue.poll() : EMPTY_BUFFER;
  }
}
