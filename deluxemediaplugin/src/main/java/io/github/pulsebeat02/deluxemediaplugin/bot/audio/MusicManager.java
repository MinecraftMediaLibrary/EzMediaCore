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

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import io.github.pulsebeat02.deluxemediaplugin.bot.DiscordLocale;
import io.github.pulsebeat02.deluxemediaplugin.bot.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MusicManager {

  private final Map<Long, MusicSendHandler> musicGuildManager;
  private final MediaBot bot;
  private final AudioPlayerManager playerManager;
  private AudioPlayer player;

  public MusicManager(@NotNull final MediaBot bot) {
    this.bot = bot;
    this.playerManager = new DefaultAudioPlayerManager();
    this.musicGuildManager = new HashMap<>();
    AudioSourceManagers.registerRemoteSources(this.playerManager);
    AudioSourceManagers.registerLocalSource(this.playerManager);
  }

  /** Join's Voice Chanel and set's log channel. */
  public void joinVoiceChannel() {
    final Guild guild = this.bot.getGuild();
    final AudioManager audio = guild.getAudioManager();
    Nill.ifSo(audio.getSendingHandler(), this::createSendingHandler);
    audio.openAudioConnection(this.bot.getChannel());
  }

  private void createSendingHandler() {
    final Guild guild = this.bot.getGuild();
    final long id = guild.getIdLong();
    this.player = this.playerManager.createPlayer();
    this.musicGuildManager.putIfAbsent(id, new MusicSendHandler(this.bot, this, this.player));
    guild.getAudioManager().setSendingHandler(this.musicGuildManager.get(id));
  }

  /** Leave's Voice Channel. */
  public void leaveVoiceChannel() {
    final Guild guild = this.bot.getGuild();
    guild.getAudioManager().closeAudioConnection();
    this.musicGuildManager.get(guild.getIdLong()).getTrackScheduler().clearQueue();
  }

  public void addTrack(@NotNull final String url) {
    this.addTrack(null, url);
  }

  /**
   * Adds track.
   *
   * @param url Load's Song.
   * @param channel Channel to send message.
   */
  public void addTrack(@Nullable final MessageChannel channel, @NotNull final String url) {
    final Guild guild = this.bot.getGuild();
    this.playerManager.loadItem(
        url,
        new AudioLoadResultHandler() {
          @Override
          public void trackLoaded(@NotNull final AudioTrack audioTrack) {
            final AudioTrackInfo info = audioTrack.getInfo();
            this.queueTrack(audioTrack, guild);
            if (channel != null) {
              channel.sendMessageEmbeds(DiscordLocale.LOADED_TRACK.build(info)).queue();
            }
          }

          private void queueTrack(
              @NotNull final AudioTrack audioTrack, @NotNull final Guild guild) {
            MusicManager.this
                .musicGuildManager
                .get(guild.getIdLong())
                .getTrackScheduler()
                .queueSong(audioTrack);
          }

          @Override
          public void playlistLoaded(@NotNull final AudioPlaylist audioPlaylist) {
            final long ms = this.calculateLength(audioPlaylist);
            this.sendEmbed(DiscordLocale.LOADED_PLAYLIST.build(ms, audioPlaylist, url));
          }

          private long calculateLength(@NotNull final AudioPlaylist audioPlaylist) {
            long ms = 0;
            for (final AudioTrack track : audioPlaylist.getTracks()) {
              this.queueTrack(track, guild);
              ms += track.getInfo().length;
            }
            return ms;
          }

          @Override
          public void noMatches() {
            this.sendEmbed(DiscordLocale.ERR_INVALID_TRACK.build(url));
          }

          @Override
          public void loadFailed(@NotNull final FriendlyException e) {
            this.sendEmbed(DiscordLocale.ERR_LAVAPLAYER.build());
          }

          private void sendEmbed(@NotNull final MessageEmbed embed) {
            if (channel != null) {
              channel.sendMessageEmbeds(embed).queue();
            }
          }
        });
  }

  public void pauseTrack() {
    if (this.player != null) {
      this.player.setPaused(true);
    }
  }

  public void resumeTrack() {
    if (this.player != null) {
      this.player.setPaused(false);
    }
  }

  public void destroyTrack() {
    if (this.player != null) {
      this.player.destroy();
    }
  }

  public @NotNull Map<Long, MusicSendHandler> getMusicGuildManager() {
    return this.musicGuildManager;
  }

  public @NotNull MediaBot getBot() {
    return this.bot;
  }

  public @NotNull AudioPlayerManager getPlayerManager() {
    return this.playerManager;
  }
}
