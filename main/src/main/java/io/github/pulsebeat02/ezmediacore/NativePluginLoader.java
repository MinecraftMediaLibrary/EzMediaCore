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
package io.github.pulsebeat02.ezmediacore;

import java.util.concurrent.CountDownLatch;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.log.LogEventListener;
import uk.co.caprica.vlcj.log.LogLevel;
import uk.co.caprica.vlcj.log.NativeLog;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class NativePluginLoader {

  private final MediaLibraryCore core;

  NativePluginLoader(@NotNull final MediaLibraryCore core) {
    this.core = core;
  }

  public void executePhantomPlayers() {
    final MediaPlayerFactory factory =
        new MediaPlayerFactory("--no-video", "--no-audio", "--verbose=0");
    final NativeLog log = this.createLog(factory);
    final EmbeddedMediaPlayer player = factory.mediaPlayers().newEmbeddedMediaPlayer();
    final CountDownLatch latch = new CountDownLatch(1);
    this.addEvents(player, latch);
    this.playMedia(player);
    this.waitMedia(latch);
    this.release(log, player, factory);
  }

  private void release(
      @NotNull final NativeLog log,
      @NotNull final EmbeddedMediaPlayer player,
      @NotNull final MediaPlayerFactory factory) {
    log.release();
    player.release();
    factory.release();
  }

  private @NotNull NativeLog createLog(@NotNull final MediaPlayerFactory factory) {
    final NativeLog log = factory.application().newLog();
    log.setLevel(LogLevel.DEBUG);
    log.addLogListener(this.createListener());
    return log;
  }

  private @NotNull LogEventListener createListener() {
    return (level, module, file, line, name, header, id, message) ->
        this.core
            .getLogger()
            .vlc("[%-20s] (%-20s) %7s: %s\n".formatted(module, name, level, message));
  }

  private void waitMedia(@NotNull final CountDownLatch latch) {
    try {
      latch.await();
    } catch (final InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  private void playMedia(@NotNull final EmbeddedMediaPlayer player) {
    player
        .media()
        .play("https://github.com/MinecraftMediaLibrary/EzMediaCore/raw/master/vlc-prerender.mp4");
  }

  private void addEvents(
      @NotNull final EmbeddedMediaPlayer player, @NotNull final CountDownLatch latch) {
    player
        .events()
        .addMediaPlayerEventListener(
            new MediaPlayerEventAdapter() {
              @Override
              public void finished(final MediaPlayer mediaPlayer) {
                latch.countDown();
              }

              @Override
              public void error(final MediaPlayer mediaPlayer) {
                latch.countDown();
              }
            });
  }
}
