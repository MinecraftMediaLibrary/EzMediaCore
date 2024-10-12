/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.dependency;

import rewrite.logging.Logger;
import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import java.util.concurrent.CountDownLatch;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.log.LogEventListener;
import uk.co.caprica.vlcj.log.LogLevel;
import uk.co.caprica.vlcj.log.NativeLog;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class NativePluginLoader {

  private static final String VLC_PRERENDER;

  static {
    VLC_PRERENDER =
        "https://github.com/MinecraftMediaLibrary/EzMediaCore/raw/master/vlc-prerender.mp4";
  }

  private final EzMediaCore core;

  NativePluginLoader( final EzMediaCore core) {
    this.core = core;
  }

  public void executePhantomPlayers() {

    final String[] args = {"--no-video", "--no-audio", "--verbose=0"};
    final MediaPlayerFactory factory = new MediaPlayerFactory(args);
    final NativeLog log = this.createLog(factory);
    final EmbeddedMediaPlayer player = factory.mediaPlayers().newEmbeddedMediaPlayer();
    final CountDownLatch latch = new CountDownLatch(1);

    this.addEvents(player, latch);
    this.playMedia(player);
    this.waitMedia(latch);
    this.release(log, player, factory);
  }

  private void release(
       final NativeLog log,
       final EmbeddedMediaPlayer player,
       final MediaPlayerFactory factory) {
    log.release();
    player.release();
    factory.release();
    this.core.getLogger().info(Locale.PLAYER_RELEASE.build());
  }

  private  NativeLog createLog( final MediaPlayerFactory factory) {

    final NativeLog log = factory.application().newLog();
    log.setLevel(LogLevel.DEBUG);
    log.addLogListener(this.createListener());

    this.core.getLogger().info(Locale.NATIVE_LOG_REGISTRATION.build());

    return log;
  }

  private  LogEventListener createListener() {
    final Logger logger = this.core.getLogger();
    return new SimpleLogEventListener(logger);
  }

  private void waitMedia( final CountDownLatch latch) {
    try {
      latch.await();
    } catch (final InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  private void playMedia( final EmbeddedMediaPlayer player) {
    player.media().play(VLC_PRERENDER);
    this.core.getLogger().info(Locale.PLAYER_START.build(VLC_PRERENDER, ""));
  }

  private void addEvents(
       final EmbeddedMediaPlayer player,  final CountDownLatch latch) {
    player.events().addMediaPlayerEventListener(new CustomMediaPlayerEventListener(latch));
  }

  private static class SimpleLogEventListener implements LogEventListener {

    private static final String LOG_FORMAT;

    static {
      LOG_FORMAT = "[%-20s] (%-20s) %7s: %s%s";
    }

    private final Logger logger;

    public SimpleLogEventListener( final Logger logger) {
      this.logger = logger;
    }

    @Override
    public void log(
        final LogLevel level,
        final String module,
        final String file,
        final Integer line,
        final String name,
        final String header,
        final Integer id,
        final String message) {
      this.logger.vlc(LOG_FORMAT.formatted(module, name, level, message, System.lineSeparator()));
    }
  }

  private static class CustomMediaPlayerEventListener extends MediaPlayerEventAdapter {

    private final CountDownLatch latch;

    private CustomMediaPlayerEventListener( final CountDownLatch latch) {
      this.latch = latch;
    }

    @Override
    public void finished( final MediaPlayer mediaPlayer) {
      this.latch.countDown();
    }

    @Override
    public void error( final MediaPlayer mediaPlayer) {
      this.latch.countDown();
    }
  }
}
