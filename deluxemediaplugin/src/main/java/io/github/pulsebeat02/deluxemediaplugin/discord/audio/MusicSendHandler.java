package io.github.pulsebeat02.deluxemediaplugin.discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

public class MusicSendHandler implements AudioSendHandler {

  private AudioFrame audioFrame;
  private final AudioPlayer audioPlayer;
  private final TrackScheduler trackScheduler;

  /**
   * MusicSendHandler Public Constructor.
   *
   * @param musicManager MusicManager Class.
   * @param audioPlayer AudioPlayer.
   * @param guild Current Guild.
   */
  public MusicSendHandler(
      @NotNull final MusicManager musicManager,
      @NotNull final AudioPlayer audioPlayer,
      @NotNull final Guild guild) {
    this.audioPlayer = audioPlayer;
    this.trackScheduler = new TrackScheduler(musicManager, audioPlayer, guild);
    audioPlayer.addListener(this.trackScheduler);
  }

  @Override
  public boolean canProvide() {
    this.audioFrame = this.audioPlayer.provide();
    return this.audioFrame != null;
  }

  @Nullable
  @Override
  public ByteBuffer provide20MsAudio() {
    return ByteBuffer.wrap(this.audioFrame.getData());
  }

  @Override
  public boolean isOpus() {
    return true;
  }

  public @NotNull AudioFrame getAudioFrame() {
    return this.audioFrame;
  }

  public @NotNull AudioPlayer getAudioPlayer() {
    return this.audioPlayer;
  }

  public @NotNull TrackScheduler getTrackScheduler() {
    return this.trackScheduler;
  }
}
