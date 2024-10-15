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
package io.github.pulsebeat02.deluxemediaplugin.bot.ffmpeg;

import rewrite.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegArguments;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegCommandExecutor;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.function.Consumer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FFmpegPipedOutput extends FFmpegCommandExecutor {

  private final String input;
  private AudioInputStream inputStream;

  public FFmpegPipedOutput(@NotNull final EzMediaCore core, @NotNull final String input) {
    super(core);
    this.input = input;
    this.addPipeArguments();
  }

  public void addPipeArguments() {
    this.addArgument(FFmpegArguments.HIDE_BANNER);
    this.addArgument(FFmpegArguments.NO_STATS);
    this.addArguments(FFmpegArguments.LOG_LEVEL, "error");
    this.addArguments(FFmpegArguments.INPUT, this.input);
    this.addArguments(FFmpegArguments.TUNE, "fastdecode");
    this.addArguments(FFmpegArguments.TUNE, "zerolatency");
    this.addArgument(FFmpegArguments.EXCLUDE_VIDEO_STREAMS);
    this.addArguments(FFmpegArguments.OUTPUT_FORMAT, "wav");
    this.addArgument(FFmpegArguments.PIPE_TO_STDOUT);
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) throws IOException {
    final ProcessBuilder builder = new ProcessBuilder(this.getArguments());
    builder.redirectError(Redirect.INHERIT);
    final Process process = builder.start();
    this.setProcess(process);
    this.handleInputStream(process);
  }

  private void handleInputStream(@NotNull final Process process) {
    try (final BufferedInputStream buffered = new BufferedInputStream(process.getInputStream())) {
      this.inputStream = AudioSystem.getAudioInputStream(buffered);
      process.waitFor();
    } catch (final UnsupportedAudioFileException | IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public @NotNull String getInput() {
    return this.input;
  }

  public @NotNull AudioInputStream getInputStream() {
    return this.inputStream;
  }
}
