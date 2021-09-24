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

package io.github.pulsebeat02.deluxemediaplugin.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.pulsebeat02.deluxemediaplugin.bot.MediaBot;
import org.jetbrains.annotations.NotNull;

/** Stolen from itxfrosty Music bot. */
public class TrackScheduler extends AudioEventAdapter {

  private final MediaBot bot;
  private final AudioPlayer audioPlayer;
  private final MusicManager musicManager;

  public TrackScheduler(
      @NotNull final MediaBot bot,
      @NotNull final MusicManager musicManager,
      @NotNull final AudioPlayer player) {
    this.bot = bot;
    this.musicManager = musicManager;
    this.audioPlayer = player;
  }

  /**
   * Queues Song.
   *
   * @param track Track to Queue.
   */
  public void queueSong(@NotNull final AudioTrack track) {
    if (this.audioPlayer.getPlayingTrack() == null) {
      this.audioPlayer.playTrack(track);
    }
  }

  /** Clear's Audio Queue. */
  public void clearQueue() {
    this.audioPlayer.stopTrack();
  }

  /** Skips Song. */
  public void skip() {
    this.audioPlayer
        .getPlayingTrack()
        .setPosition(this.audioPlayer.getPlayingTrack().getDuration());
  }

  /**
   * Check's if Audio is paused.
   *
   * @return If Paused.
   */
  public boolean isPaused() {
    return this.audioPlayer.isPaused();
  }

  /**
   * Set's the Audio to paused parameter.
   *
   * @param paused Pause or not.
   */
  public void setPaused(final boolean paused) {
    this.audioPlayer.setPaused(paused);
  }

  /**
   * Set's Volume of Audio Player.
   *
   * @param volume Volume to set Audio player.
   */
  public void setVolume(final int volume) {
    this.audioPlayer.setVolume(volume);
  }

  public @NotNull AudioPlayer getAudioPlayer() {
    return this.audioPlayer;
  }

  public @NotNull MusicManager getMusicManager() {
    return this.musicManager;
  }
}
