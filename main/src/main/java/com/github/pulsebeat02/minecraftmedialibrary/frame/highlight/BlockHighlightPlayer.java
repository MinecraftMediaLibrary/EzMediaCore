package com.github.pulsebeat02.minecraftmedialibrary.frame.highlight;

import com.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.frame.VideoPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;

/**
 * A VLCJ integrated player used to play videos in Minecraft. The library uses a callback for the
 * specific function from native libraries. It renders it in debug highlights.
 */
public class BlockHighlightPlayer extends VideoPlayer {

  /**
   * Instantiates a new BlockHighlightPlayer.
   *
   * @param library the library
   * @param url the url
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public BlockHighlightPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String url,
      @NotNull final BlockHighlightCallbackPrototype callback,
      final int width,
      final int height) {
    super(library, url, width, height, callback);
    Logger.info(String.format("Created a Debug Highlight Integrated Video Player (%s)", url));
  }

  /**
   * Instantiates a new BlockHighlightPlayer.
   *
   * @param library the library
   * @param file the file
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public BlockHighlightPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final Path file,
      @NotNull final BlockHighlightCallbackPrototype callback,
      final int width,
      final int height) {
    super(library, file, width, height, callback);
    Logger.info(
        String.format(
            "Created a Debug Highlight Integrated Video Player (%s)", file.toAbsolutePath()));
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
   * Starts player.
   *
   * @param players which players to play the audio for
   */
  @Override
  public void start(@NotNull final Collection<? extends Player> players) {
    super.start(players);
  }

  /** Releases the media player. */
  @Override
  public void release() {
    super.release();
  }

  /** The type Builder. */
  public static class Builder {

    private String url;
    private int width = 15;
    private int height = 15;
    private BlockHighlightCallbackPrototype callback;

    private Builder() {}

    public Builder setUrl(final String url) {
      this.url = url;
      return this;
    }

    public Builder setWidth(final int width) {
      this.width = width;
      return this;
    }

    public Builder setHeight(final int height) {
      this.height = height;
      return this;
    }

    public Builder setCallback(final BlockHighlightCallbackPrototype callback) {
      this.callback = callback;
      return this;
    }

    public BlockHighlightPlayer build(@NotNull final MediaLibrary library) {
      return new BlockHighlightPlayer(library, url, callback, width, height);
    }
  }
}
