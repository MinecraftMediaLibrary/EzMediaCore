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
import com.google.common.base.Strings;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.LinuxVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.OsxVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.WindowsVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * The main abstract class for VideoPlayer classes to extend. Frame Callbacks and Video Players MUST
 * have the correct classes associated with each other! An example of the classes that are linked
 * together can be found below:
 *
 * <p>ChatCallback - ChatPlayer
 *
 * <p>EntityCloudCallback - EntityPlayer
 *
 * <p>GifIntegratedPlayer - [In Development]
 *
 * <p>BlockHighlightCallback - BlockHighlightPlayer
 *
 * <p>MapDataCallback - MapPlayer
 *
 * <p>ParallelVideoPlayer - [In Development]
 *
 * <p>ScoreboardCallback - ScoreboardPlayer
 *
 * <p>The VLCVideoPlayer directly uses VLC if it is supported in the platform. This includes Windows
 * and MacOS operating systems. However, for Linux systems that are not supported (or any other odd
 * ball operating systems), you have to use the JavaCVVideoPlayer players which are slower but very
 * compatible.
 */
public abstract class VLCVideoPlayer implements VideoPlayerContext {

  private final MediaLibrary library;
  private final VideoSurfaceAdapter adapter;
  private final MinecraftVideoRenderCallback renderCallback;
  private final String url;
  private final FrameCallback callback;
  private final String sound;
  private final Collection<Player> watchers;

  private EmbeddedMediaPlayer mediaPlayerComponent;
  private boolean playing;
  private int frameRate;
  private int width;
  private int height;

  /**
   * Instantiates a new Abstract video player.
   *
   * @param library the library
   * @param url the url
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public VLCVideoPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String type,
      @NotNull final String url,
      final int width,
      final int height,
      @NotNull final FrameCallback callback) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL cannot be empty or null!");
    Preconditions.checkArgument(width > 0, String.format("Width is not valid! (%d)", width));
    Preconditions.checkArgument(height > 0, String.format("Height is not valid! (%d)", height));
    this.library = library;
    this.url = url;
    this.width = width;
    this.height = height;
    this.callback = callback;
    renderCallback = new MinecraftVideoRenderCallback(this);
    adapter =
        RuntimeUtilities.isWindows()
            ? new WindowsVideoSurfaceAdapter()
            : RuntimeUtilities.isMac()
                ? new OsxVideoSurfaceAdapter()
                : new LinuxVideoSurfaceAdapter();
    sound = getLibrary().getPlugin().getName().toLowerCase();
    watchers = new ArrayList<>();
    initializePlayer();
    Logger.info(String.format("Created a VLC Integrated %s Video Player (%s)", type, url));
  }

  /**
   * Instantiates a new Abstract video player.
   *
   * @param library the library
   * @param file the file
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public VLCVideoPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String type,
      @NotNull final Path file,
      final int width,
      final int height,
      @NotNull final FrameCallback callback) {
    this(library, type, file.toAbsolutePath().toString(), width, height, callback);
  }

  private void initializePlayer() {
    mediaPlayerComponent =
        new MediaPlayerFactory(
                frameRate != 0
                    ? new String[] {String.format("--fps-fps=%d", frameRate)}
                    : new String[] {})
            .mediaPlayers()
            .newEmbeddedMediaPlayer();
    mediaPlayerComponent
        .videoSurface()
        .set(
            new CallbackVideoSurface(
                new BufferFormatCallback() {
                  @Override
                  public BufferFormat getBufferFormat(
                      final int sourceWidth, final int sourceHeight) {
                    return new RV32BufferFormat(width, height);
                  }

                  @Override
                  public void allocatedBuffers(final ByteBuffer[] buffers) {}
                },
                renderCallback,
                false,
                adapter));
    mediaPlayerComponent.audio().setMute(true);
  }

  @Override
  public MediaLibrary getLibrary() {
    return library;
  }

  @Override
  public String getUrl() {
    return url;
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
    if (mediaPlayerComponent == null) {
      initializePlayer();
    }
    mediaPlayerComponent.media().play(url);
    for (final Player p : players) {
      p.playSound(p.getLocation(), sound, SoundCategory.MUSIC, 100.0F, 1.0F);
    }
    watchers.addAll(players);
    Logger.info(String.format("Started Playing the Video! (%s)", url));
  }

  @Override
  public void stop(@NotNull final Collection<? extends Player> players) {
    playing = false;
    mediaPlayerComponent.controls().stop();
    for (final Player p : players) {
      p.stopSound(sound);
    }
    Logger.info(String.format("Stopped Playing the Video! (%s)", url));
  }

  @Override
  public void release() {
    playing = false;
    mediaPlayerComponent.release();
    mediaPlayerComponent = null;
    Logger.info(String.format("Released the Video! (%s)", url));
  }

  @Override
  public void resume(@NotNull final Collection<? extends Player> players) {
    playing = true;
    if (mediaPlayerComponent == null) {
      initializePlayer();
    }
    mediaPlayerComponent.controls().start();
    for (final Player p : players) {
      p.playSound(p.getLocation(), sound, SoundCategory.MUSIC, 100.0F, 1.0F);
    }
    Logger.info(String.format("Resumed the Video! (%s)", url));
  }

  @Override
  public void setRepeat(final boolean setting) {
    mediaPlayerComponent.controls().setRepeat(setting);
    if (setting) {
      mediaPlayerComponent
          .events()
          .addMediaPlayerEventListener(
              new MediaPlayerEventAdapter() {
                @Override
                public void finished(@NotNull final MediaPlayer mediaPlayer) {
                  playAudio(watchers);
                }
              });
    }
    Logger.info(String.format("Set Setting Loop to (%s)! (%s)", setting, url));
  }

  @Override
  public boolean isPlaying() {
    return playing;
  }

  @Override
  public int getFrameRate() {
    return frameRate;
  }

  @Override
  public void setFrameRate(final int frameRate) {
    this.frameRate = frameRate;
  }

  /**
   * Gets the MediaPlayerComponent.
   *
   * @return the MediaPlayerComponent
   */
  public EmbeddedMediaPlayer getMediaPlayerComponent() {
    return mediaPlayerComponent;
  }

  /**
   * Gets the adapter.
   *
   * @return the video surface adapter
   */
  public VideoSurfaceAdapter getAdapter() {
    return adapter;
  }

  /**
   * Gets the MinecraftVideoRenderCallback.
   *
   * @return the Minecraft render callback
   */
  public MinecraftVideoRenderCallback getRenderCallback() {
    return renderCallback;
  }

  private void playAudio(@NotNull final Collection<? extends Player> players) {
    for (final Player p : players) {
      p.playSound(p.getLocation(), sound, 1.0F, 1.0F);
    }
  }

  private static class MinecraftVideoRenderCallback extends RenderCallbackAdapter {

    private final Consumer<int[]> callback;

    /**
     * Instantiates a new MinecraftVideoRenderCallback.
     *
     * @param player the VideoPlayer
     */
    public MinecraftVideoRenderCallback(@NotNull final VLCVideoPlayer player) {
      super(new int[player.getWidth() * player.getHeight()]);
      callback = player.getCallback()::send;
    }

    /**
     * Displays the image data.
     *
     * @param mediaPlayer the media player
     * @param buffer the buffer
     */
    @Override
    protected void onDisplay(final MediaPlayer mediaPlayer, final int[] buffer) {
      callback.accept(buffer);
    }

    /**
     * Gets the callback for this render.
     *
     * @return the callback for the image data.
     */
    public Consumer<int[]> getCallback() {
      return callback;
    }
  }
}
