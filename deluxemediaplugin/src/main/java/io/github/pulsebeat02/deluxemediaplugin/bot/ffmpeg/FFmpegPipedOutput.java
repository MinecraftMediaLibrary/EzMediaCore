package io.github.pulsebeat02.deluxemediaplugin.bot.ffmpeg;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegArguments;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegCommandExecutor;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;

public final class FFmpegPipedOutput extends FFmpegCommandExecutor {

  private final String input;
  private AudioInputStream inputStream;

  public FFmpegPipedOutput(@NotNull final MediaLibraryCore core, @NotNull final String input) {
    super(core);
    this.input = input;
    this.addPipeArguments();
  }

  public void addPipeArguments() {
    this.addArgument(FFmpegArguments.HIDE_BANNER);
    this.addArgument(FFmpegArguments.NO_STATS);
    this.addArgument(FFmpegArguments.OVERWRITE_FILE);
    this.addArguments(FFmpegArguments.INPUT, this.input);
    this.addArgument(FFmpegArguments.EXCLUDE_VIDEO_STREAMS);
    this.addArguments(FFmpegArguments.OUTPUT_FORMAT, "wav");
    this.addArgument(FFmpegArguments.PIPE_OUTPUT.formatted("1"));
  }

  @Override
  public void log(@NotNull final String line) {
  }

  @Override
  public void onAfterExecution() {
    final Process process = this.getProcess();
    try (final BufferedInputStream buffered = new BufferedInputStream(process.getInputStream())) {
      this.inputStream = AudioSystem.getAudioInputStream(buffered);
    } catch (final UnsupportedAudioFileException | IOException e) {
      throw new AssertionError(e);
    }
  }

  public @NotNull String getInput() {
    return this.input;
  }

  public @NotNull AudioInputStream getInputStream() {
    return this.inputStream;
  }
}
