package io.github.pulsebeat02.deluxemediaplugin.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import io.github.pulsebeat02.deluxemediaplugin.bot.locale.DiscordLocale;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MusicResultHandler implements AudioLoadResultHandler {

  private final MusicManager manager;
  private final Guild guild;
  private final MessageChannel channel;
  private final String url;

  public MusicResultHandler(
      @NotNull final MusicManager manager,
      @Nullable final MessageChannel channel,
      @NotNull final String url) {
    this.manager = manager;
    this.channel = channel;
    this.guild = manager.getBot().getGuild();
    this.url = url;
  }

  @Override
  public void trackLoaded(@NotNull final AudioTrack audioTrack) {
    final AudioTrackInfo info = audioTrack.getInfo();
    this.queueTrack(audioTrack, this.guild);
    if (this.channel != null) {
      this.channel.sendMessageEmbeds(DiscordLocale.LOADED_TRACK.build(info)).queue();
    }
  }

  private void queueTrack(@NotNull final AudioTrack audioTrack, @NotNull final Guild guild) {
    this.manager
        .getMusicGuildManager()
        .get(guild.getIdLong())
        .getTrackScheduler()
        .queueSong(audioTrack);
  }

  @Override
  public void playlistLoaded(@NotNull final AudioPlaylist audioPlaylist) {
    final long ms = this.calculateLength(audioPlaylist);
    this.sendEmbed(DiscordLocale.LOADED_PLAYLIST.build(ms, audioPlaylist, this.url));
  }

  private long calculateLength(@NotNull final AudioPlaylist audioPlaylist) {
    long ms = 0;
    for (final AudioTrack track : audioPlaylist.getTracks()) {
      this.queueTrack(track, this.guild);
      ms += track.getInfo().length;
    }
    return ms;
  }

  @Override
  public void noMatches() {
    this.sendEmbed(DiscordLocale.ERR_INVALID_TRACK.build(this.url));
  }

  @Override
  public void loadFailed(@NotNull final FriendlyException e) {
    this.sendEmbed(DiscordLocale.ERR_LAVAPLAYER.build());
  }

  private void sendEmbed(@NotNull final MessageEmbed embed) {
    if (this.channel != null) {
      this.channel.sendMessageEmbeds(embed).queue();
    }
  }
}
