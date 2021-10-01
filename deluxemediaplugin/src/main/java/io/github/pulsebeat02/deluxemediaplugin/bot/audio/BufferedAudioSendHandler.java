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
