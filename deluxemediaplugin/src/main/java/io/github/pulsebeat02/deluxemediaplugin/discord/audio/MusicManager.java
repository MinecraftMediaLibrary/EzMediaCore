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
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import io.github.pulsebeat02.deluxemediaplugin.discord.MediaBot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

public class MusicManager {

  private static final SimpleDateFormat HOURS_MINUTES_SECONDS;

  static {
    HOURS_MINUTES_SECONDS = new SimpleDateFormat("mm:ss:SSS");
  }

  private final Map<Long, MusicSendHandler> musicGuildManager;
  private final MediaBot bot;
  private final AudioPlayerManager playerManager;

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
    final long id = guild.getIdLong();
    final VoiceChannel voiceChannel = this.bot.getChannel();
    final AudioManager audio = guild.getAudioManager();
    if (audio.getSendingHandler() == null) {
      this.musicGuildManager.putIfAbsent(
          id, new MusicSendHandler(this.bot, this, this.playerManager.createPlayer()));
      audio.setSendingHandler(this.musicGuildManager.get(id));
    }
    audio.openAudioConnection(voiceChannel);
  }

  /** Leave's Voice Channel. */
  public void leaveVoiceChannel() {
    final Guild guild = this.bot.getGuild();
    guild.getAudioManager().closeAudioConnection();
    this.musicGuildManager.get(guild.getIdLong()).getTrackScheduler().clearQueue();
  }

  /**
   * Adds track.
   *
   * @param url Load's Song.
   * @param channel Channel to send message.
   */
  public void addTrack(@NotNull final MessageChannel channel, @NotNull final String url) {
    final Guild guild = this.bot.getGuild();
    this.playerManager.loadItem(
        url,
        new AudioLoadResultHandler() {
          @Override
          public void trackLoaded(final AudioTrack audioTrack) {
            final AudioTrackInfo info = audioTrack.getInfo();
            MusicManager.this
                .musicGuildManager
                .get(guild.getIdLong())
                .getTrackScheduler()
                .queueSong(audioTrack);
            channel
                .sendMessageEmbeds(
                    new EmbedBuilder()
                        .setTitle(info.title)
                        .addField("Author", info.author, false)
                        .addField(
                            "Playtime Length",
                            HOURS_MINUTES_SECONDS.format(new Date(info.length)),
                            false)
                        .addField("Stream", info.isStream ? "Yes" : "No", false)
                        .build())
                .queue();
          }

          @Override
          public void playlistLoaded(final AudioPlaylist audioPlaylist) {
            long ms = 0;
            for (final AudioTrack track : audioPlaylist.getTracks()) {
              MusicManager.this
                  .musicGuildManager
                  .get(guild.getIdLong())
                  .getTrackScheduler()
                  .queueSong(track);
              ms += track.getInfo().length;
            }
            channel
                .sendMessageEmbeds(
                    new EmbedBuilder()
                        .setTitle(audioPlaylist.getName())
                        .addField(
                            "Playtime Length", HOURS_MINUTES_SECONDS.format(new Date(ms)), false)
                        .build())
                .queue();
          }

          @Override
          public void noMatches() {
            channel
                .sendMessageEmbeds(
                    new EmbedBuilder()
                        .setTitle("Media Error")
                        .setDescription("Could not find song!")
                        .build())
                .queue();
          }

          @Override
          public void loadFailed(final FriendlyException e) {
            channel
                .sendMessageEmbeds(
                    new EmbedBuilder()
                        .setTitle("Severe Player Error Occurred!")
                        .setDescription(
                            "An error occurred! Check console for possible exceptions or warnings.")
                        .build())
                .queue();
          }
        });
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
