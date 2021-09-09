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

  private final AudioPlayerManager playerManager;
  public final Map<Long, MusicSendHandler> musicGuildManager;

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
