package io.github.pulsebeat02.deluxemediaplugin.bot.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BufferedAudioSendHandler implements AudioSendHandler {

  private final Queue<ByteBuffer> packets;
  private final ByteArrayOutputStream output;
  private final InputStream input;

  public BufferedAudioSendHandler(@NotNull final String url) throws IOException {
    this.packets = new ConcurrentLinkedQueue<>();
    this.output = new ByteArrayOutputStream();
    this.input = new URL(url).openStream();
  }

  public void startListening() {
    CompletableFuture.runAsync(
        () -> {
          try {
            final byte[] chunk = new byte[4096];
            int bytesRead;
            while ((bytesRead = this.input.read(chunk)) > 0) {
              this.output.write(chunk, 0, bytesRead);
            }
            this.packets.add(ByteBuffer.wrap(chunk));
          } catch (final IOException e) {
            e.printStackTrace();
          }
        });
  }

  @Override
  public boolean canProvide() {
    return this.packets.size() < 15;
  }

  @Nullable
  @Override
  public ByteBuffer provide20MsAudio() {
    return this.packets.poll();
  }

  @Override
  public boolean isOpus() {
    return false;
  }
}
