package io.github.pulsebeat02.deluxemediaplugin.bot.ffmpeg;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegArguments;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegCommandExecutor;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.function.Consumer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FFmpegPipedOutput extends FFmpegCommandExecutor {

  private final String input;
  private AudioInputStream inputStream;

  public FFmpegPipedOutput(@NotNull final MediaLibraryCore core, @NotNull final String input) {
    super(core);
    this.input = input;
    this.addPipeArguments();
  }

  public void addPipeArguments() {
    this.addArguments(FFmpegArguments.INPUT, this.input);
    this.addArgument(FFmpegArguments.EXCLUDE_VIDEO_STREAMS);
    this.addArguments(FFmpegArguments.OUTPUT_FORMAT, "wav");
    this.addArgument(FFmpegArguments.PIPE_TO_STDOUT);
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) throws IOException {
    final ProcessBuilder builder = new ProcessBuilder(this.getArguments());
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
