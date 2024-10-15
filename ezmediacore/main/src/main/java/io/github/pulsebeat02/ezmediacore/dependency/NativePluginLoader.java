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

import java.util.concurrent.CountDownLatch;

import uk.co.caprica.vlcj.factory.MediaPlayerApi;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.EventApi;
import uk.co.caprica.vlcj.player.base.MediaApi;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class NativePluginLoader {

  private static final String VLC_PRERENDER = "https://github.com/MinecraftMediaLibrary/EzMediaCore/raw/master/vlc-prerender.mp4";
  private static final String[] VLC_ARGS = {"--no-video", "--no-audio", "--verbose=0"};

  public void executePhantomPlayers() {
    final MediaPlayerFactory factory = new MediaPlayerFactory(VLC_ARGS);
    final MediaPlayerApi api = factory.mediaPlayers();
    final EmbeddedMediaPlayer player = api.newEmbeddedMediaPlayer();
    final CountDownLatch latch = new CountDownLatch(1);
    this.addEvents(player, latch);
    this.playMedia(player);
    this.waitMedia(latch);
    this.release(player, factory);
  }

  private void release(
       final EmbeddedMediaPlayer player,
       final MediaPlayerFactory factory) {
    player.release();
    factory.release();
  }


  private void waitMedia( final CountDownLatch latch) {
    try {
      latch.await();
    } catch (final InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  private void playMedia( final EmbeddedMediaPlayer player) {
    final MediaApi api = player.media();
    api.play(VLC_PRERENDER);
  }

  private void addEvents(
       final EmbeddedMediaPlayer player,  final CountDownLatch latch) {
    final CustomMediaPlayerEventListener listener = new CustomMediaPlayerEventListener(latch);
    final EventApi events = player.events();
    events.addMediaPlayerEventListener(listener);
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
