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

package io.github.pulsebeat02.minecraftmedialibrary.frame.gif;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.frame.FrameCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.VLCVideoPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.utility.FileUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.ImageUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GIFPlayer extends VLCVideoPlayer {

  private final List<BufferedImage> images;
  private final float frameDuration;
  private ScheduledExecutorService scheduler;
  private int index;

  /**
   * Instantiates a new GIF video player.
   *
   * @param library the library
   * @param file the file
   * @param width the width
   * @param height the height
   * @param callback the callback
   * @deprecated uses File
   */
  @Deprecated
  public GIFPlayer(
      final @NotNull MediaLibrary library,
      final @NotNull Path file,
      final int width,
      final int height,
      final FrameCallback callback) {
    super(library, "Gif", file, width, height, callback);
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
    final FrameCallback callback = getCallback();
    final int size = images.size();
    final int delay = (int) (frameDuration * 1000);
    scheduler.scheduleAtFixedRate(
        () -> {
          if (index >= size) {
            scheduler.shutdown();
          }
          callback.send(VideoUtilities.getBuffer(images.get(index)));
          index++;
        },
        0,
        delay,
        TimeUnit.MILLISECONDS);
  }

  /** Stops the Gif player. */
  @Override
  public void stop(@NotNull final Collection<? extends Player> players) {
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
    private int width = 5;
    private int height = 5;
    private FrameCallback callback;

    private Builder() {}

    /**
     * Sets url.
     *
     * @param url the url
     * @return the url
     */
    public Builder url(@NotNull final String url) {
      this.url = url;
      return this;
    }

    /**
     * Sets width.
     *
     * @param width the width
     * @return the width
     */
    public Builder width(final int width) {
      this.width = width;
      return this;
    }

    /**
     * Sets height.
     *
     * @param height the height
     * @return the height
     */
    public Builder height(final int height) {
      this.height = height;
      return this;
    }

    /**
     * Sets callback.
     *
     * @param callback the callback
     * @return the callback
     */
    public Builder callback(@NotNull final FrameCallback callback) {
      this.callback = callback;
      return this;
    }

    /**
     * Create vlcj integrated player vlcj integrated player.
     *
     * @param library the library
     * @return the vlcj integrated player
     */
    public GIFPlayer build(@NotNull final MediaLibrary library) {
      if (PathUtilities.isValidPath(url)) {
        return new GIFPlayer(library, Paths.get(url), width, height, callback);
      } else {
        final Path downloaded = library.getImageFolder().resolve(FilenameUtils.getName(url));
        FileUtilities.copyURLToFile(url, downloaded);
        return new GIFPlayer(library, downloaded, width, height, callback);
      }
    }
  }
}
