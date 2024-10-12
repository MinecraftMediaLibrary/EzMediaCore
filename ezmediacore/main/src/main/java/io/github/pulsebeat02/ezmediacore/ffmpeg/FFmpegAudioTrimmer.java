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
import io.github.pulsebeat02.ezmediacore.format.FormatterProvider;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jetbrains.annotations.Contract;


public class FFmpegAudioTrimmer extends FFmpegCommandExecutor implements AudioTrimmer {

  public static final SimpleDateFormat FFMPEG_TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");

  private final String input;
  private final String output;
  private final long ms;

  FFmpegAudioTrimmer(
       final EzMediaCore core,
       final String input,
       final String output,
      final long ms) {
    super(core);
    this.input = input;
    this.output = output;
    this.ms = ms;
    this.clearArguments();
    this.generateArguments();
  }

  @Contract("_, _, _, _ -> new")
  public static  FFmpegAudioTrimmer ofFFmpegAudioTrimmer(
       final EzMediaCore core,
       final String input,
       final String output,
      final long ms) {
    return new FFmpegAudioTrimmer(core, input, output, ms);
  }

  @Contract("_, _, _, _ -> new")
  public static  FFmpegAudioTrimmer ofFFmpegAudioTrimmer(
       final EzMediaCore core,
       final Path input,
       final Path output,
      final long ms) {
    return ofFFmpegAudioTrimmer(core, input.toString(), output.toString(), ms);
  }

  @Contract("_, _, _, _ -> new")
  public static  FFmpegAudioTrimmer ofFFmpegAudioTrimmer(
       final EzMediaCore core,
       final Path input,
       final String fileName,
      final long ms) {
    return ofFFmpegAudioTrimmer(core, input, core.getAudioPath().resolve(fileName), ms);
  }

  private void generateArguments() {

    final String path = this.getCore().getFFmpegPath().toString();
    this.addArgument(path);

    this.addArgument(FFmpegArguments.HIDE_BANNER);
    this.addArgument(FFmpegArguments.NO_STATS);
    this.addArguments(FFmpegArguments.LOG_LEVEL, "error");
    this.addArguments(FFmpegArguments.INPUT, this.input);

    final String time = FFMPEG_TIME_FORMATTER.format(new Date(this.ms));
    this.addArguments(FFmpegArguments.DURATION_START, time);

    this.addArgument(this.output);
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
  public long getStartTime() {
    return this.ms;
  }
}
