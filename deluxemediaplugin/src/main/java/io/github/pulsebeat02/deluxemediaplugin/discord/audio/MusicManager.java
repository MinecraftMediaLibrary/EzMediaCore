package io.github.pulsebeat02.deluxemediaplugin.discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;
import java.util.Map;

public class MusicManager {

	private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

	public final Map<Long, MusicSendHandler> musicGuildManager = new HashMap<>();

	public MusicManager() {
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	/**
	 * Join's Voice Chanel and set's log channel.
	 *
	 * @param voiceChannel Voice Channel.
	 * @param guild        Guild.
	 */
	public void joinVoiceChannel(VoiceChannel voiceChannel, Guild guild) {
		AudioManager audio = guild.getAudioManager();

		if (audio.getSendingHandler() == null) {
			musicGuildManager.putIfAbsent(guild.getIdLong(), new MusicSendHandler(this, playerManager.createPlayer(), guild));
			audio.setSendingHandler(musicGuildManager.get(guild.getIdLong()));
		}

		audio.openAudioConnection(voiceChannel);
	}

	/**
	 * Leave's Voice Channel.
	 *
	 * @param guild Guild
	 */
	public void leaveVoiceChannel(Guild guild) {
		guild.getAudioManager().closeAudioConnection();
		musicGuildManager.get(guild.getIdLong()).getTrackScheduler().clearQueue();
	}

	/**
	 * Adds track.
	 *
	 * @param user    User to issued the adding of the track.
	 * @param url     Load's Song.
	 * @param channel Channel to send message.
	 * @param guild   Guild.
	 */
	public void addTrack(User user, String url, MessageChannel channel, Guild guild) {
		playerManager.loadItem(url, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack audioTrack) {
				musicGuildManager.get(guild.getIdLong()).getTrackScheduler().queueSong(audioTrack);
			}

			@Override
			public void playlistLoaded(AudioPlaylist audioPlaylist) {
				for (AudioTrack track : audioPlaylist.getTracks()) {
					musicGuildManager.get(guild.getIdLong()).getTrackScheduler().queueSong(track);
				}
			}

			@Override
			public void noMatches() {
				channel.sendMessageEmbeds(new EmbedBuilder().setDescription("Could not find song!").build()).queue();
			}

			@Override
			public void loadFailed(FriendlyException e) {
				channel.sendMessageEmbeds(new EmbedBuilder().setDescription("An error occurred!").build()).queue();
			}
		});
	}

	public Map<Long, MusicSendHandler> getMusicGuildManager() {
		return musicGuildManager;
	}
}