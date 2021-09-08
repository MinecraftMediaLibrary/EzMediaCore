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
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.log.LogLevel;
import uk.co.caprica.vlcj.log.NativeLog;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class NativePluginLoader {

  public NativePluginLoader() {}

  public void executePhantomPlayers() {

    // loads all necessary VLC plugins before actual playback occurs

    final MediaPlayerFactory factory = new MediaPlayerFactory("--no-video", "--no-audio");
    final EmbeddedMediaPlayer player = factory.mediaPlayers().newEmbeddedMediaPlayer();
    final CountDownLatch latch = new CountDownLatch(1);

    final NativeLog logger = factory.application().newLog();
    if (logger == null) { // ignore this warning as its intellij being dumb with native bindings
      Logger.info("VLC Native Logger not available on this platform!");
      return;
    } else {
      logger.setLevel(LogLevel.DEBUG);
      logger.addLogListener(
          (level, module, file, line, name, header, id, message) ->
              Logger.directPrintVLC(
                  "[%-20s] (%-20s) %7s: %s\n".formatted(module, name, level, message)));
    }

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

    player
        .media()
        .play("https://github.com/MinecraftMediaLibrary/EzMediaCore/raw/master/vlc-prerender.mp4");

    try {
      latch.await();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }

    logger.release();
    player.release();
    factory.release();
  }
}
