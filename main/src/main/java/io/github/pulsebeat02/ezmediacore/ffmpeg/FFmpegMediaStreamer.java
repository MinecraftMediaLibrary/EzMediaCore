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
package io.github.pulsebeat02.ezmediacore.ffmpeg;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.rtp.RTPStreamingServer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FFmpegMediaStreamer extends FFmpegCommandExecutor
    implements MediaServer {

  private final RTPStreamingServer server;
  private final String input;
  private final String output;

  public FFmpegMediaStreamer(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String input,
      @NotNull final String ip,
      final int port) {
    super(core);
    this.input = input;
    this.output = "rtsp://localhost:8554/live.stream";
    this.clearArguments();
    this.addMultipleArguments(this.generateArguments(configuration));
    this.server = new RTPStreamingServer(core, ip, port);
  }

  @Contract("_ -> new")
  private @NotNull List<String> generateArguments(@NotNull final AudioConfiguration configuration) {
    return new ArrayList<>(
        List.of(
            this.getCore().getFFmpegPath().toString(),
            "-i",
            this.input,
            "-nostdin",
            "-ab",
            String.valueOf(configuration.getBitrate()),
            "-ac",
            String.valueOf(configuration.getChannels()),
            "-ar",
            String.valueOf(configuration.getSamplingRate()),
            "-vol",
            String.valueOf(configuration.getVolume()),
            "-ss",
            String.valueOf(configuration.getStartTime()),
            "-f",
            "rtsp",
            "-rtsp_transport",
            "tcp"));
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) {
    this.server.executeAsync();
    this.addArguments("-max_muxing_queue_size", "9999");
    this.addArgument(this.output);
    super.executeWithLogging(logger);
  }

  @Override
  public void log(final String line) {
    this.getCore().getLogger().ffmpegStream(line);
  }

  @Override
  public @NotNull String getInput() {
    return this.input;
  }

  @Override
  public @NotNull String getOutput() {
    return this.output;
  }

  @Override
  public void close() {
    super.close();
    if (this.server != null) {
      this.server.close();
    }
  }
}
