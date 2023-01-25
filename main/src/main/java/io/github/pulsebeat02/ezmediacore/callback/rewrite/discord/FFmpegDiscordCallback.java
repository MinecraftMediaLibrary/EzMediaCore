package io.github.pulsebeat02.ezmediacore.callback.rewrite.discord;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.audio.FFmpegDiscordCallbackHandle;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.buffered.FFmpegMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.FFmpegPlayerOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.StdoutFFmpegOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

/*
 * FFmpeg audio stream from stdout is exposed to user, so they can read from it and
 * play it into the Discord bot.
 */
public final class FFmpegDiscordCallback extends DiscordCallback implements FFmpegDiscordCallbackHandle {

  private AudioInputStream stream;

  FFmpegDiscordCallback(@NotNull final MediaLibraryCore core) {
    super(core);
  }

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {
    final FFmpegMediaPlayer ffmpeg = (FFmpegMediaPlayer) player;
    final FFmpegPlayerOutput output = (FFmpegPlayerOutput) ffmpeg.getOutput();
    final StdoutFFmpegOutput std = output.getStdout();
    final InputStream input = std.getResultingOutput().getRaw();
    try {
      this.stream = AudioSystem.getAudioInputStream(input);
    } catch (final UnsupportedAudioFileException | IOException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public @Nullable AudioInputStream getStream() {
    return this.stream;
  }

  @Override
  public void process(final byte @NotNull [] data) {}
}
