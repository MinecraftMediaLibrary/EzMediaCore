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
import com.github.pulsebeat02.minecraftmedialibrary.utility.ImageUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GifIntegratedPlayer extends VideoPlayerBase {

  private final List<BufferedImage> images;
  private final float frameDuration;
  private ScheduledExecutorService scheduler;
  private int index;

  /**
   * Instantiates a new Abstract video player.
   *
   * @param library the library
   * @param url the url
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public GifIntegratedPlayer(
      final @NotNull MinecraftMediaLibrary library,
      final @NotNull String url,
      final int width,
      final int height,
      final @NotNull Consumer<int[]> callback) {
    super(library, url, width, height, callback);
    final File downloaded = new File(library.getImageFolder().toFile(), FilenameUtils.getName(url));
    try {
      FileUtils.copyURLToFile(new URL(url), downloaded);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    images = ImageUtilities.getFrames(downloaded);
    frameDuration = ImageUtilities.getGifFrameDelay(downloaded);
    scheduler = Executors.newScheduledThreadPool(1);
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
  public GifIntegratedPlayer(
      final @NotNull MinecraftMediaLibrary library,
      final @NotNull File file,
      final int width,
      final int height,
      final @NotNull Consumer<int[]> callback) {
    super(library, file, width, height, callback);
    images = ImageUtilities.getFrames(file);
    frameDuration = ImageUtilities.getGifFrameDelay(file);
    scheduler = Executors.newScheduledThreadPool(1);
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
   * Starts the Gif player.
   *
   * @param players which players to play the audio for
   */
  @Override
  public void start(final @NotNull Collection<? extends Player> players) {
    final Consumer<int[]> callback = getCallback();
    final int size = images.size();
    final int delay = (int) (frameDuration * 1000);
    scheduler.scheduleAtFixedRate(
        () -> {
          if (index >= size) {
            scheduler.shutdown();
          }
          callback.accept(VideoUtilities.getBuffer(images.get(index)));
          index++;
        },
        0,
        delay,
        TimeUnit.MILLISECONDS);
  }

  /** Stops the Gif player. */
  @Override
  public void stop() {
    scheduler.shutdown();
  }

  /** Releases the Gif player. */
  @Override
  public void release() {
    scheduler = null;
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
    public GifIntegratedPlayer createVLCJIntegratedPlayer(
        @NotNull final MinecraftMediaLibrary library) {
      return new GifIntegratedPlayer(library, url, width, height, callback);
    }
  }
}
