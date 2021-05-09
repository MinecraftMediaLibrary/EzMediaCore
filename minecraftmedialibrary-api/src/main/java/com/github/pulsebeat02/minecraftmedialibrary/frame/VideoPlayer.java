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

package com.github.pulsebeat02.minecraftmedialibrary.frame;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
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
import java.util.Collection;
import java.util.function.Consumer;

/**
 * The main abstract class for VideoPlayer classes to extend. Frame Callbacks and Video Players MUST
 * have the correct classes associated with each other! An example of the classes that are linked
 * together can be found below:
 *
 * <p>ChatCallback -> ChatIntegratedPlayer
 *
 * <p>EntityCloudCallback -> EntityCloudIntegratedPlayer
 *
 * <p>GifIntegratedPlayer ->
 *
 * <p>BlockHighlightCallback -> BlockHighlightPlayer
 *
 * <p>MapDataCallback -> MapIntegratedPlayer
 *
 * <p>ParallelVideoPlayer
 *
 * <p>ScoreboardCallback -> ScoreboardIntegratedPlayer
 */
public abstract class VideoPlayer {

  private final MinecraftMediaLibrary library;
  private final VideoSurfaceAdapter adapter;
  private final MinecraftVideoRenderCallback renderCallback;
  private final String url;
  private final FrameCallback callback;
  private final String sound;

  private EmbeddedMediaPlayer mediaPlayerComponent;
  private boolean playing;
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
  public VideoPlayer(
      @NotNull final MinecraftMediaLibrary library,
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
    initializePlayer();
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
  public VideoPlayer(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final Path file,
      final int width,
      final int height,
      @NotNull final FrameCallback callback) {
    this(library, file.toAbsolutePath().toString(), width, height, callback);
  }

  private void initializePlayer() {
    mediaPlayerComponent = new MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
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
    mediaPlayerComponent.audio().mute();
  }

  /**
   * Gets library.
   *
   * @return the library
   */
  public MinecraftMediaLibrary getLibrary() {
    return library;
  }

  /**
   * Gets url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Gets width.
   *
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * Sets width.
   *
   * @param width the width
   */
  public void setWidth(final int width) {
    this.width = width;
  }

  /**
   * Gets height.
   *
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets height.
   *
   * @param height the height
   */
  public void setHeight(final int height) {
    this.height = height;
  }

  /**
   * Gets callback.
   *
   * @return the callback
   */
  public FrameCallback getCallback() {
    return callback;
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

  /**
   * Gets the sound name for the resourcepack.
   *
   * @return the sound name
   */
  public String getSound() {
    return sound;
  }

  /**
   * Starts the player.
   *
   * @param players which players to play the audio for
   */
  public void start(@NotNull final Collection<? extends Player> players) {
    playing = true;
    if (mediaPlayerComponent == null) {
      initializePlayer();
    }
    mediaPlayerComponent.media().play(url);
    for (final Player p : players) {
      p.playSound(p.getLocation(), sound, 1.0F, 1.0F);
    }
    Logger.info(String.format("Started Playing the Video! (%s)", url));
  }

  /** Stops the player. */
  public void stop(@NotNull final Collection<? extends Player> players) {
    playing = false;
    mediaPlayerComponent.controls().stop();
    for (final Player p : players) {
      p.stopSound(sound);
    }
    Logger.info(String.format("Stopped Playing the Video! (%s)", url));
  }

  /** Releases the player. */
  public void release() {
    playing = false;
    mediaPlayerComponent.release();
    mediaPlayerComponent = null;
    Logger.info(String.format("Released the Video! (%s)", url));
  }

  /**
   * Repeats the player.
   *
   * @param setting the setting
   */
  public void setRepeat(final boolean setting) {
    mediaPlayerComponent.controls().setRepeat(setting);
    Logger.info(String.format("Set Setting Loop to (%s)! (%s)", setting, url));
  }

  /**
   * Returns whether the video is playing.
   *
   * @return whether the video is playing or not
   */
  public boolean isPlaying() {
    return playing;
  }

  private static class MinecraftVideoRenderCallback extends RenderCallbackAdapter {

    private final Consumer<int[]> callback;

    /**
     * Instantiates a new MinecraftVideoRenderCallback.
     *
     * @param player the VideoPlayer
     */
    public MinecraftVideoRenderCallback(@NotNull final VideoPlayer player) {
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
