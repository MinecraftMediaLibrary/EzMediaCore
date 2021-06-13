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

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResultFuture;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.Input;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.ffmpeg.FFmpegDependencyInstallation;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
public abstract class JaffreeVideoPlayer extends VideoPlayer {

  private FFmpeg ffmpeg;
  private FFmpegResultFuture future;

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
  public JaffreeVideoPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String type,
      @NotNull final String url,
      final int width,
      final int height,
      @NotNull final FrameCallback callback) {
    super(library, url, width, height, callback);
    initializePlayer(0);
    Logger.info(String.format("Created an FFmpeg Integrated %s Video Player (%s)", type, url));
  }

  private void initializePlayer(final long seconds) {
    final String url = getUrl();
    final FrameCallback callback = getCallback();
    final Input input;
    final Path path = Paths.get(url);
    final long ms = seconds * 1000;
    if (Files.exists(path)) {
      input = UrlInput.fromPath(path).setPosition(ms);
    } else {
      input = UrlInput.fromUrl(url).setPosition(ms);
    }
    final int width = getWidth();
    final int height = getHeight();
    final int frameRate = getFrameRate();
    ffmpeg =
        new FFmpeg(FFmpegDependencyInstallation.getFFmpegPath())
            .addInput(input)
            .addOutput(
                FrameOutput.withConsumer(
                        new FrameConsumer() {
                          @Override
                          public void consumeStreams(final List<Stream> streams) {}

                          @Override
                          public void consume(final Frame frame) {
                            if (frame == null) {
                              return;
                            }
                            callback.send(
                                frame.getImage().getRGB(0, 0, width, height, null, 0, width));
                          }
                        })
                    .setFrameRate(frameRate)
                    .disableStream(StreamType.AUDIO)
                    .disableStream(StreamType.SUBTITLE)
                    .disableStream(StreamType.DATA));
  }

  @Override
  public void start(@NotNull final Collection<? extends Player> players) {
    setPlaying(true);
    if (ffmpeg == null) {
      initializePlayer(0);
    }
    CompletableFuture.runAsync(
        () -> {
          do {
            future = ffmpeg.executeAsync();
            setStart(System.currentTimeMillis());
            playAudio(players);
            try {
              future.toCompletableFuture().get();
            } catch (final InterruptedException | ExecutionException e) {
              e.printStackTrace();
            }
          } while (isRepeat());
        });
    Logger.info(String.format("Started Playing the Video! (%s)", getUrl()));
  }

  @Override
  public void stop(@NotNull final Collection<? extends Player> players) {
    final String sound = getSound();
    setPlaying(false);
    if (ffmpeg != null) {
      future.graceStop();
    }
    for (final Player p : players) {
      p.stopSound(sound);
    }
    Logger.info(String.format("Stopped Playing the Video! (%s)", getUrl()));
  }

  @Override
  public void release() {
    setPlaying(false);
    if (ffmpeg != null) {
      future.graceStop();
      ffmpeg = null;
    }
    Logger.info(String.format("Released the Video! (%s)", getUrl()));
  }

  @Override
  public void resume(@NotNull final Collection<? extends Player> players) {
    setPlaying(true);
    CompletableFuture.runAsync(
        () -> {
          do {
            initializePlayer(getElapsedTime());
            future = ffmpeg.executeAsync();
            playAudio(players);
            try {
              future.toCompletableFuture().get();
            } catch (final InterruptedException | ExecutionException e) {
              e.printStackTrace();
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
