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

package com.github.pulsebeat02.minecraftmedialibrary.video.player;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.WindowsVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * A VLCJ integrated player used to play videos in Minecraft. The library uses a callback for the
 * specific function from native libraries.
 */
public class VLCJIntegratedPlayer extends VideoPlayerBase {

  private final EmbeddedMediaPlayer mediaPlayerComponent;

  /**
   * Instantiates a new Vlcj integrated player.
   *
   * @param library the library
   * @param url the url
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public VLCJIntegratedPlayer(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final String url,
      final int width,
      final int height,
      @NotNull final Consumer<int[]> callback) {
    super(library, url, width, height, callback);
    mediaPlayerComponent = new MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
    final BufferFormatCallback bufferFormatCallback =
        new BufferFormatCallback() {
          @Override
          public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
            return new RV32BufferFormat(getWidth(), getHeight());
          }

          @Override
          public void allocatedBuffers(final ByteBuffer[] buffers) {}
        };
    final CallbackVideoSurface surface =
        new CallbackVideoSurface(
            bufferFormatCallback,
            new MinecraftRenderCallback(),
            false,
            new WindowsVideoSurfaceAdapter());
    mediaPlayerComponent.videoSurface().set(surface);
    Logger.info(String.format("Created a VLCJ Integrated Video Player (%s)", url));
  }

  /**
   * Instantiates a new VLCJIntegratedPlayer.
   *
   * @param library the library
   * @param file the file
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public VLCJIntegratedPlayer(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final File file,
      final int width,
      final int height,
      @NotNull final Consumer<int[]> callback) {
    super(library, file.getAbsolutePath(), width, height, callback);
    mediaPlayerComponent = new MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
    final BufferFormatCallback bufferFormatCallback =
        new BufferFormatCallback() {
          @Override
          public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
            return new RV32BufferFormat(getWidth(), getHeight());
          }

          @Override
          public void allocatedBuffers(final ByteBuffer[] buffers) {}
        };
    final CallbackVideoSurface surface =
        new CallbackVideoSurface(
            bufferFormatCallback,
            new MinecraftRenderCallback(),
            false,
            new WindowsVideoSurfaceAdapter());
    mediaPlayerComponent.videoSurface().set(surface);
    mediaPlayerComponent.audio().mute();
    Logger.info(
        String.format("Created a VLCJ Integrated Video Player (%s)", file.getAbsolutePath()));
  }

  /**
   * Returns a new builder class to use.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Starts playing the video.
   *
   * @param players which players should hear the audio
   */
  @Override
  public void start(@NotNull final Collection<? extends Player> players) {
    final String url = getUrl();
    mediaPlayerComponent.media().play(url);
    for (final Player p : players) {
      p.playSound(p.getLocation(), getLibrary().getPlugin().getName().toLowerCase(), 1.0F, 1.0F);
    }
    Logger.info(String.format("Started Playing Video! (%s)", url));
  }

  /** Stops playing the video. */
  @Override
  public void stop() {
    mediaPlayerComponent.controls().stop();
    Logger.info(String.format("Stopped Playing Video! (%s)", getUrl()));
  }

  /** Releases the media player. */
  @Override
  public void release() {
    mediaPlayerComponent.release();
  }

  public void setRepeat(final boolean setting) {
    mediaPlayerComponent.controls().setRepeat(setting);
  }

  /**
   * Gets media player component.
   *
   * @return the media player component
   */
  public EmbeddedMediaPlayer getMediaPlayerComponent() {
    return mediaPlayerComponent;
  }

  /** The type Builder. */
  public static class Builder {

    private String url;
    private int width;
    private int height;
    private Consumer<int[]> callback;

    private Builder() {}

    /**
     * Sets url.
     *
     * @param url the url
     * @return the url
     */
    public Builder setUrl(@NotNull final String url) {
      this.url = url;
      return this;
    }

    /**
     * Sets width.
     *
     * @param width the width
     * @return the width
     */
    public Builder setWidth(final int width) {
      this.width = width;
      return this;
    }

    /**
     * Sets height.
     *
     * @param height the height
     * @return the height
     */
    public Builder setHeight(final int height) {
      this.height = height;
      return this;
    }

    /**
     * Sets callback.
     *
     * @param callback the callback
     * @return the callback
     */
    public Builder setCallback(@NotNull final Consumer<int[]> callback) {
      this.callback = callback;
      return this;
    }

    /**
     * Create vlcj integrated player vlcj integrated player.
     *
     * @param library the library
     * @return the vlcj integrated player
     */
    public VLCJIntegratedPlayer createVLCJIntegratedPlayer(
        @NotNull final MinecraftMediaLibrary library) {
      return new VLCJIntegratedPlayer(library, url, width, height, callback);
    }
  }

  private class MinecraftRenderCallback extends RenderCallbackAdapter {

    private MinecraftRenderCallback() {
      super(new int[getWidth() * getHeight()]);
    }

    @Override
    protected void onDisplay(final MediaPlayer mediaPlayer, final int[] buffer) {
      getCallback().accept(buffer);
    }
  }
}
