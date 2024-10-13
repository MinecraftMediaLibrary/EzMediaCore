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
package io.github.pulsebeat02.ezmediacore.ffmpeg;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import rewrite.rtp.RTPServer;
import java.io.IOException;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;



public class FFmpegMediaStreamer extends FFmpegCommandExecutor implements MediaServer {

  private final RTPServer server;
  private final String input;
  private final String output;

  FFmpegMediaStreamer(
       final EzMediaCore core,
       final AudioConfiguration configuration,
       final String input,
       final String ip,
      final int port) {
    super(core);
    this.input = input;
    this.output = "rtsp://localhost:8554/live.stream";
    this.clearArguments();
    this.generateArguments(configuration);
    this.server = RTPServer.ofRtpServer(core, ip, port);
  }

  @Contract("_, _, _, _, _ -> new")
  public static  FFmpegMediaStreamer ofFFmpegMediaStreamer(
       final EzMediaCore core,
       final AudioConfiguration configuration,
       final String input,
       final String ip,
      final int port) {
    return new FFmpegMediaStreamer(core, configuration, input, ip, port);
  }

  private void generateArguments( final AudioConfiguration configuration) {

    final String path = this.getCore().getFFmpegPath().toString();
    this.addArgument(path);

    this.addArgument(FFmpegArguments.HIDE_BANNER);
    this.addArgument(FFmpegArguments.NO_STATS);
    this.addArguments(FFmpegArguments.LOG_LEVEL, "error");
    this.addArgument(FFmpegArguments.NATIVE_FRAME_READ_RATE);
    this.addArguments(FFmpegArguments.INPUT, this.input);
    this.addArgument(FFmpegArguments.NO_CONSOLE_INPUT);
    this.addArguments(FFmpegArguments.TUNE, "fastdecode");
    this.addArguments(FFmpegArguments.TUNE, "zerolatency");

    final String bitrate = String.valueOf(configuration.getBitrate());
    this.addArguments(FFmpegArguments.AUDIO_BITRATE, bitrate);

    final String channels = String.valueOf(configuration.getChannels());
    this.addArguments(FFmpegArguments.AUDIO_CHANNELS, channels);

    final String sampling = String.valueOf(configuration.getSamplingRate());
    this.addArguments(FFmpegArguments.AUDIO_SAMPLING, sampling);

    final String volume = String.valueOf(configuration.getVolume());
    this.addArguments(FFmpegArguments.AUDIO_VOLUME, volume);

    final String start = String.valueOf(configuration.getStartTime());
    this.addArguments(FFmpegArguments.DURATION_START, start);
  }

  @Override
  public void executeWithLogging( final Consumer<String> logger) throws IOException {
    this.server.executeAsync(ExecutorProvider.RTSP_SERVER);
    this.addArguments(FFmpegArguments.OUTPUT_FORMAT, "rtsp");
    this.addArgument("-rtsp_transport");
    this.addArgument("tcp");
    this.addArguments("-max_muxing_queue_size", "9999");
    this.addArgument(this.output);
    super.executeWithLogging(logger);
  }

  @Override
  public void log(final  String line) {
    this.getCore().getLogger().ffmpegStream(line);
  }

  @Override
  public  String getInput() {
    return this.input;
  }

  @Override
  public  String getOutput() {
    return this.output;
  }

  @Override
  public void close() throws InterruptedException {
    super.close();
    if (this.server != null) {
      this.server.close();
    }
  }
}
