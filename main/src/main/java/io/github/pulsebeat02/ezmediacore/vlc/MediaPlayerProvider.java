package io.github.pulsebeat02.ezmediacore.vlc;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class MediaPlayerProvider {

  public static final MediaPlayerFactory MEDIA_PLAYER_FACTORY;
  public static EmbeddedMediaPlayer EMBEDDED_MEDIA_PLAYER;

  static {
    MEDIA_PLAYER_FACTORY = new MediaPlayerFactory("--no-audio");
    EMBEDDED_MEDIA_PLAYER = MEDIA_PLAYER_FACTORY.mediaPlayers().newEmbeddedMediaPlayer();
  }
}
