/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.frame;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.entity.Player;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
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
 * <p>The JavaCVVideoPlayer uses JavaCV (a Java implementation of OpenCV) mainly used for Linux or
 * any other odd ball operating systems that don't have VLC supported. If you are on a Windows or
 * MacOS operating system, it is strongly recommended to use the VLCVideoPlayer players instead as
 * they are much faster and coded in C/C++ libraries.
 */
public abstract class JavaCVVideoPlayer implements VideoPlayerContext {

  private final MediaLibrary library;
  private final Object arg;
  private final FrameCallback callback;
  private final String sound;
  private final FrameGrabberType type;
  private FrameGrabber grabber;

  private boolean playing;
  private boolean repeat;
  private int width;
  private int height;

  /**
   * Instantiates a new Abstract video player.
   *
   * @param library the library
   * @param type the type of video playback
   * @param arg the argument
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public JavaCVVideoPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final FrameGrabberType type,
      @NotNull final Object arg,
      final int width,
      final int height,
      @NotNull final FrameCallback callback) {
    Preconditions.checkArgument(width > 0, String.format("Width is not valid! (%d)", width));
    Preconditions.checkArgument(height > 0, String.format("Height is not valid! (%d)", height));
    this.library = library;
    this.arg = arg;
    this.width = width;
    this.height = height;
    this.callback = callback;
    this.type = type;
    sound = getLibrary().getPlugin().getName().toLowerCase();
    initializePlayer();
  }

  private void initializePlayer() {
    switch (type) {
      case NORMAL_VIDEO:
        grabber = new OpenCVFrameGrabber((String) arg);
        break;
      case CAMERA_OUTPUT:
        grabber = new OpenCVFrameGrabber((int) arg);
        break;
      default:
        grabber = new OpenCVFrameGrabber((String) arg);
        break;
    }
  }

  @Override
  public MediaLibrary getLibrary() {
    return library;
  }

  @Override
  public String getUrl() {
    return arg.toString();
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public void setWidth(final int width) {
    this.width = width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public void setHeight(final int height) {
    this.height = height;
  }

  @Override
  public FrameCallback getCallback() {
    return callback;
  }

  @Override
  public String getSound() {
    return sound;
  }

  @Override
  public void start(@NotNull final Collection<? extends Player> players) {
    playing = true;
    if (grabber == null) {
      initializePlayer();
    }
    CompletableFuture.runAsync(
            () -> {
              try {
                grabber.start();
                Frame frame;
                while ((frame = grabber.grab()) != null) {
                  final int width = frame.imageWidth;
                  final int[] rgb =
                      ImageIO.read(new ByteArrayInputStream(frame.data.array()))
                          .getRGB(0, 0, width, frame.imageHeight, null, 0, width);
                  callback.send(rgb);
                }
              } catch (final IOException e) {
                e.printStackTrace();
              }
            })
        .whenCompleteAsync(
            (t, throwable) -> {
              if (repeat) {
                try {
                  grabber.restart();
                } catch (final FrameGrabber.Exception e) {
                  e.printStackTrace();
                }
                playAudio(players);
              }
            });
    playAudio(players);
    Logger.info(String.format("Started Playing the Video! (%s)", arg));
  }

  @Override
  public void stop(@NotNull final Collection<? extends Player> players) {
    playing = false;
    if (grabber != null) {
      try {
        grabber.stop();
      } catch (final FrameGrabber.Exception e) {
        e.printStackTrace();
      }
    }
    for (final Player p : players) {
      p.stopSound(sound);
    }
    Logger.info(String.format("Stopped Playing the Video! (%s)", arg));
  }

  @Override
  public void release() {
    playing = false;
    if (grabber != null) {
      try {
        grabber.stop();
        grabber.release();
      } catch (final FrameGrabber.Exception e) {
        e.printStackTrace();
      }
      grabber = null;
    }
    Logger.info(String.format("Released the Video! (%s)", arg));
  }

  @Override
  public void setRepeat(final boolean setting) {
    repeat = true;
    Logger.info(String.format("Set Setting Loop to (%s)! (%s)", setting, arg));
  }

  private void playAudio(@NotNull final Collection<? extends Player> players) {
    for (final Player p : players) {
      p.playSound(p.getLocation(), sound, 1.0F, 1.0F);
    }
  }

  @Override
  public boolean isPlaying() {
    return playing;
  }
}
