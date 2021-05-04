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

package com.github.pulsebeat02.minecraftmedialibrary.frame.entity;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.frame.VideoPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

/**
 * A VLCJ integrated player used to play videos in Minecraft. The library uses a callback for the
 * specific function from native libraries. It renders it on entities.
 */
public class EntityCloudIntegratedPlayer extends VideoPlayer {

  private final Location location;
  private final Entity[] entities;

  /**
   * Instantiates a new EntityCloudIntegratedPlayer.
   *
   * @param library the library
   * @param url the url
   * @param width the width
   * @param height the height
   * @param callback the callback
   * @param location the location
   */
  public EntityCloudIntegratedPlayer(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final String url,
      @NotNull final EntityCloudCallback callback,
      @NotNull final Location location,
      final int width,
      final int height) {
    super(library, url, width, height, callback);
    this.location = location;
    entities = callback.getEntities();
    Logger.info(String.format("Created a VLCJ Integrated Entity Cloud Video Player (%s)", url));
  }

  /**
   * Instantiates a new EntityCloudIntegratedPlayer.
   *
   * @param library the library
   * @param file the file
   * @param width the width
   * @param height the height
   * @param callback the callback
   * @param location the location
   */
  public EntityCloudIntegratedPlayer(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final Path file,
      @NotNull final EntityCloudCallback callback,
      @NotNull final Location location,
      final int width,
      final int height) {
    super(library, file, width, height, callback);
    this.location = location;
    entities = callback.getEntities();
    Logger.info(
        String.format("Created a VLCJ Integrated Video Player (%s)", file.toAbsolutePath()));
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
    if (entities != null) {
      for (final Entity entity : entities) {
        entity.remove();
      }
    }
    super.release();
  }

  /**
   * Gets the location of the player.
   *
   * @return the location
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Gets the entity array.
   *
   * @return the entity array
   */
  public Entity[] getEntities() {
    return entities;
  }

  /** The type Builder. */
  public static class Builder {

    private String url;
    private int width = 5;
    private int height = 5;
    private EntityCloudCallback callback;
    private Location location;

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

    public Builder setCallback(final EntityCloudCallback callback) {
      this.callback = callback;
      return this;
    }

    public Builder setLocation(final Location location) {
      this.location = location;
      return this;
    }

    public EntityCloudIntegratedPlayer build(@NotNull final MinecraftMediaLibrary library) {
      return new EntityCloudIntegratedPlayer(library, url, callback, location, width, height);
    }
  }
}
