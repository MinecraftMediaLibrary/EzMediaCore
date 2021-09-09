package io.github.pulsebeat02.deluxemediaplugin.discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Stolen from itxfrosty Music bot.
 */
public class TrackScheduler extends AudioEventAdapter {

	private final Guild guild;
	private final AudioPlayer audioPlayer;
	private final MusicManager musicManager;

	public TrackScheduler(final MusicManager musicManager, AudioPlayer player, Guild guild) {
		this.musicManager = musicManager;
		this.audioPlayer = player;
		this.guild = guild;
	}

	/**
	 * Queues Song.
	 *
	 * @param track Track to Queue.
	 */
	public void queueSong(AudioTrack track) {
		if (audioPlayer.getPlayingTrack() == null) audioPlayer.playTrack(track);
	}

	/**
	 * Clear's Audio Queue.
	 */
	public void clearQueue() {
		audioPlayer.stopTrack();
	}

	/**
	 * Skips Song.
	 */
	public void skip() {
		audioPlayer.getPlayingTrack().setPosition(audioPlayer.getPlayingTrack().getDuration());
	}

	/**
	 * Check's if Audio is paused.
	 *
	 * @return If Paused.
	 */
	public boolean isPaused() {
		return audioPlayer.isPaused();
	}

	/**
	 * Set's the Audio to paused parameter.
	 *
	 * @param paused Pause or not.
	 */
	public void setPaused(boolean paused) {
		audioPlayer.setPaused(paused);
	}

	/**
	 * Set's Volume of Audio Player.
	 *
	 * @param volume Volume to set Audio player.
	 */
	public void setVolume(int volume) {
		audioPlayer.setVolume(volume);
	}

	public AudioPlayer getAudioPlayer() {
		return audioPlayer;
	}

	public MusicManager getMusicManager() {
		return musicManager;
	}
}