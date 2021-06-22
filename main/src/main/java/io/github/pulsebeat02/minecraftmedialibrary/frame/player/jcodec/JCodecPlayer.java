/*.........................................................................................
 . Copyright © 2021 Brandon Li
 .                                                                                        .
 . Permission is hereby granted, free of charge, to any person obtaining a copy of this
 . software and associated documentation files (the “Software”), to deal in the Software
 . without restriction, including without limitation the rights to use, copy, modify, merge,
 . publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 . persons to whom the Software is furnished to do so, subject to the following conditions:
 .
 . The above copyright notice and this permission notice shall be included in all copies
 . or substantial portions of the Software.
 .
 . THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 . EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 . MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 . NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 . BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 . ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 . CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 .  SOFTWARE.
 .                                                                                        .
 .........................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.frame.player.jcodec;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.FrameCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.VideoPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities;
import org.bukkit.entity.Player;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Size;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * The main abstract class for VideoPlayer classes to extend. Frame Callbacks and Video Players MUST
 * have the correct classes associated with each other! An example of the classes that are linked
 * together can be found below:
 *
 * <p>ChatCallback - LinuxChatPlayer
 *
 * <p>EntityCloudCallback - LinuxEntityPlayer
 *
 * <p>GifIntegratedPlayer - [In Development]
 *
 * <p>BlockHighlightCallback - LinuxBlockHighlightPlayer
 *
 * <p>MapDataCallback - LinuxMapPlayer
 *
 * <p>ParallelVideoPlayer - [In Development]
 *
 * <p>ScoreboardCallback - LinuxScoreboardPlayer
 *
 * <p>The JCodecPlayer uses JCodec (a pure Java implementation of media playback) mainly used for
 * Linux or any other odd ball operating systems that don't have VLC or FFmpeg supported. If you are
 * on a Windows or MacOS operating system, it is strongly recommended to use the VLCPlayer players
 * instead as they are much faster and coded in C/C++ libraries.
 */
public class JCodecPlayer extends VideoPlayer {

  private FrameGrab grab;

  /**
   * Instantiates a new Abstract video player.
   *
   * @param library the library
   * @param url the url
   * @param width the width
   * @param height the height
   * @param callback the callback
   * @param type the type of video player
   */
  public JCodecPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String type,
      @NotNull final String url,
      final int width,
      final int height,
      @NotNull final FrameCallback callback) {
    super(library, url, width, height, callback);
    initializePlayer(0);
    Logger.info(String.format("Created an JCodec Integrated %s Video Player (%s)", type, url));
  }

  private void initializePlayer(final long seconds) {
    final String url = getUrl();
    final FrameCallback callback = getCallback();
    setStart(seconds * 1000);
    try {
      grab = FrameGrab.createFrameGrab(NIOUtils.readableFileChannel(getUrl()));
      grab.seekToSecondPrecise(seconds);
      grab.getMediaInfo().setDim(new Size(getWidth(), getHeight()));
    } catch (final IOException | JCodecException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void start(@NotNull final Collection<? extends Player> players) {
    setPlaying(true);
    if (grab == null) {
      initializePlayer(0);
    }
    final FrameCallback callback = getCallback();
    final int width = getWidth();
    final int height = getHeight();
    CompletableFuture.runAsync(
        () -> {
          do {
            Picture picture;
            while (isPlaying()) {
              try {
                if ((picture = grab.getNativeFrame()) == null) {
                  break;
                }
                callback.send(
                    VideoUtilities.toBufferedImage(picture)
                        .getRGB(0, 0, width, height, null, 0, width));
              } catch (final IOException e) {
                e.printStackTrace();
              }
            }
          } while (isRepeat());
        });
    Logger.info(String.format("Started Playing the Video! (%s)", getUrl()));
  }

  @Override
  public void stop(@NotNull final Collection<? extends Player> players) {
    final String sound = getSound();
    setPlaying(false);
    for (final Player p : players) {
      p.stopSound(sound);
    }
    Logger.info(String.format("Stopped Playing the Video! (%s)", getUrl()));
  }

  @Override
  public void release() {
    setPlaying(false);
    if (grab != null) {
      grab = null;
    }
    Logger.info(String.format("Released the Video! (%s)", getUrl()));
  }

  @Override
  public void resume(@NotNull final Collection<? extends Player> players) {
    setPlaying(true);
    final FrameCallback callback = getCallback();
    final int width = getWidth();
    final int height = getHeight();
    CompletableFuture.runAsync(
        () -> {
          initializePlayer(getElapsedTime());
          playAudio(players);
          do {
            Picture picture;
            while (isPlaying()) {
              try {
                if ((picture = grab.getNativeFrame()) == null) {
                  break;
                }
                callback.send(
                    VideoUtilities.toBufferedImage(picture)
                        .getRGB(0, 0, width, height, null, 0, width));
              } catch (final IOException e) {
                e.printStackTrace();
              }
            }
          } while (isRepeat());
        });
    Logger.info(String.format("Resumed the Video! (%s)", getUrl()));
  }

  @Override
  public long getElapsedTime() {
    return (int) (System.currentTimeMillis() - getStart()) / 1000;
  }

  @Override
  public void setRepeat(final boolean setting) {
    super.setRepeat(true);
    Logger.info(String.format("Set Setting Loop to (%s)! (%s)", setting, getUrl()));
  }

  private void playAudio(@NotNull final Collection<? extends Player> players) {
    final String sound = getSound();
    for (final Player p : players) {
      p.playSound(p.getLocation(), sound, 1.0F, 1.0F);
    }
  }
}
