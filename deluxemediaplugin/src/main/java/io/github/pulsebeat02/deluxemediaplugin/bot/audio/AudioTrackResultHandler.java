package io.github.pulsebeat02.deluxemediaplugin.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import io.github.pulsebeat02.deluxemediaplugin.bot.locale.DiscordLocale;
import io.github.pulsebeat02.deluxemediaplugin.utility.mutable.MutableLong;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;

public final class AudioTrackResultHandler implements AudioLoadResultHandler {

  private final MusicManager manager;
  private final MessageChannel channel;
  private final String url;
  private final TrackScheduler scheduler;

  AudioTrackResultHandler(
      @NotNull final MusicManager manager,
      @NotNull final MessageChannel channel,
      @NotNull final String url) {
    this.manager = manager;
    this.scheduler = this.getScheduler();
    this.channel = channel;
    this.url = url;
  }

  private @NotNull TrackScheduler getScheduler() {
    return this.manager
        .getMusicGuildManager()
        .get(this.manager.getBot().getGuild().getIdLong())
        .getTrackScheduler();
  }

  @Override
  public void trackLoaded(final @NotNull AudioTrack audioTrack) {
    final AudioTrackInfo info = audioTrack.getInfo();
    this.scheduler.queueSong(audioTrack);
    Nill.ifNot(this.channel, () -> this.sendTrackLoadedMessage(info));
  }

  private void sendTrackLoadedMessage(@NotNull final AudioTrackInfo info) {
    this.channel.sendMessageEmbeds(DiscordLocale.LOADED_TRACK.build(info)).queue();
  }

  @Override
  public void playlistLoaded(final @NotNull AudioPlaylist audioPlaylist) {
    final MutableLong ms = MutableLong.ofLong(0L);
    for (final AudioTrack track : audioPlaylist.getTracks()) {
      this.scheduler.queueSong(track);
      ms.add(track.getInfo().length);
    }
    Nill.ifNot(this.channel, () -> this.sendPlaylistLoadedMessage(audioPlaylist, ms));
  }

  private void sendPlaylistLoadedMessage(
      @NotNull final AudioPlaylist audioPlaylist, @NotNull final MutableLong ms) {
    this.channel
        .sendMessageEmbeds(
            DiscordLocale.LOADED_PLAYLIST.build(ms.getNumber(), audioPlaylist, this.url))
        .queue();
  }

  @Override
  public void noMatches() {
    Nill.ifNot(this.channel, this::sendNoMatchMessage);
  }

  private void sendNoMatchMessage() {
    this.channel.sendMessageEmbeds(DiscordLocale.ERR_INVALID_TRACK.build(this.url)).queue();
  }

  @Override
  public void loadFailed(final FriendlyException e) {
    Nill.ifNot(this.channel, this::sendLoadFailedMessage);
  }

  private void sendLoadFailedMessage() {
    this.channel.sendMessageEmbeds(DiscordLocale.ERR_LAVAPLAYER.build()).queue();
  }
}
