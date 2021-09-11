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

package io.github.pulsebeat02.deluxemediaplugin.discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

public class MusicSendHandler implements AudioSendHandler {

  private final AudioPlayer audioPlayer;
  private final TrackScheduler trackScheduler;
  private AudioFrame audioFrame;

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
