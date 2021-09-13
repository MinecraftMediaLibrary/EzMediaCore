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

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

public class MusicManager {

  public final Map<Long, MusicSendHandler> musicGuildManager;
  private final AudioPlayerManager playerManager;

  public MusicManager() {
    this.playerManager = new DefaultAudioPlayerManager();
    this.musicGuildManager = new HashMap<>();
    AudioSourceManagers.registerRemoteSources(this.playerManager);
    AudioSourceManagers.registerLocalSource(this.playerManager);
  }

  /**
   * Join's Voice Chanel and set's log channel.
   *
   * @param voiceChannel Voice Channel.
   * @param guild Guild.
   */
  public void joinVoiceChannel(
      @NotNull final VoiceChannel voiceChannel, @NotNull final Guild guild) {
    final AudioManager audio = guild.getAudioManager();
    if (audio.getSendingHandler() == null) {
      this.musicGuildManager.putIfAbsent(
          guild.getIdLong(), new MusicSendHandler(this, this.playerManager.createPlayer(), guild));
      audio.setSendingHandler(this.musicGuildManager.get(guild.getIdLong()));
    }
    audio.openAudioConnection(voiceChannel);
  }

  /**
   * Leave's Voice Channel.
   *
   * @param guild Guild
   */
  public void leaveVoiceChannel(@NotNull final Guild guild) {
    guild.getAudioManager().closeAudioConnection();
    this.musicGuildManager.get(guild.getIdLong()).getTrackScheduler().clearQueue();
  }

  /**
   * Adds track.
   *
   * @param user User to issued the adding of the track.
   * @param url Load's Song.
   * @param channel Channel to send message.
   * @param guild Guild.
   */
  public void addTrack(
      @NotNull final User user,
      @NotNull final String url,
      @NotNull final MessageChannel channel,
      @NotNull final Guild guild) {
    this.playerManager.loadItem(
        url,
        new AudioLoadResultHandler() {
          @Override
          public void trackLoaded(final AudioTrack audioTrack) {
            MusicManager.this
                .musicGuildManager
                .get(guild.getIdLong())
                .getTrackScheduler()
                .queueSong(audioTrack);
          }

          @Override
          public void playlistLoaded(final AudioPlaylist audioPlaylist) {
            for (final AudioTrack track : audioPlaylist.getTracks()) {
              MusicManager.this
                  .musicGuildManager
                  .get(guild.getIdLong())
                  .getTrackScheduler()
                  .queueSong(track);
            }
          }

          @Override
          public void noMatches() {
            channel
                .sendMessageEmbeds(
                    new EmbedBuilder().setDescription("Could not find song!").build())
                .queue();
          }

          @Override
          public void loadFailed(final FriendlyException e) {
            channel
                .sendMessageEmbeds(new EmbedBuilder().setDescription("An error occurred!").build())
                .queue();
          }
        });
  }

  public @NotNull Map<Long, MusicSendHandler> getMusicGuildManager() {
    return this.musicGuildManager;
  }
}
