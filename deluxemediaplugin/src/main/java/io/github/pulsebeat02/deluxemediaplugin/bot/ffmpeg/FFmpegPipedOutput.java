package io.github.pulsebeat02.deluxemediaplugin.bot.ffmpeg;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegArguments;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegCommandExecutor;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.jetbrains.annotations.NotNull;

public class FFmpegPipedOutput extends FFmpegCommandExecutor {

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
    this.addArguments(FFmpegArguments.OUTPUT_FORMAT, "mp3");
    this.addArgument(FFmpegArguments.PIPE_OUTPUT.formatted("1"));
  }

  @Override
  public void log(final @NotNull String line) {
  }

  @Override
  public void onAfterExecution() {
    super.onAfterExecution();
    final Process process = this.getProcess();
    final InputStream stream = process.getInputStream();
    try {
      this.inputStream = AudioSystem.getAudioInputStream(stream);
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
