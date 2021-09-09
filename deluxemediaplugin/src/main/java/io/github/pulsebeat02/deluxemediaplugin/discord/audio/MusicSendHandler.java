package io.github.pulsebeat02.deluxemediaplugin.discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public class MusicSendHandler implements AudioSendHandler {

	private AudioFrame audioFrame;
	private final AudioPlayer audioPlayer;
	private final TrackScheduler trackScheduler;

	/**
	 * MusicSendHandler Public Constructor.
	 *
	 * @param musicManager MusicManager Class.
	 * @param audioPlayer  AudioPlayer.
	 * @param guild        Current Guild.
	 */
	public MusicSendHandler(final MusicManager musicManager, AudioPlayer audioPlayer, Guild guild) {
		this.audioPlayer = audioPlayer;
		trackScheduler = new TrackScheduler(musicManager, audioPlayer, guild);

		audioPlayer.addListener(trackScheduler);
	}

	@Override
	public boolean canProvide() {
		audioFrame = audioPlayer.provide();
		return audioFrame != null;
	}

	@Nullable
	@Override
	public ByteBuffer provide20MsAudio() {
		return ByteBuffer.wrap(audioFrame.getData());
	}

	@Override
	public boolean isOpus() {
		return true;
	}

	public AudioFrame getAudioFrame() {
		return audioFrame;
	}

	public AudioPlayer getAudioPlayer() {
		return audioPlayer;
	}

	public TrackScheduler getTrackScheduler() {
		return trackScheduler;
	}
}