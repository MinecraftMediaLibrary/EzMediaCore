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
import com.github.pulsebeat02.minecraftmedialibrary.frame.FrameCallback;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** The callback used for clouds to update entity clouds for each frame when necessary. */
public final class EntityCloudCallback implements FrameCallback {

  private final MinecraftMediaLibrary library;
  private final UUID[] viewers;
  private final Location location;
  private final Entity[] entities;
  private final int videoWidth;
  private final int delay;
  private final int width;
  private final int height;
  private long lastUpdated;

  /**
   * Instantiates a new EntityCloudCallback.
   *
   * @param library the library
   * @param viewers the viewers
   * @param location the location
   * @param width the width
   * @param height the height
   * @param videoWidth the video width
   * @param delay the delay
   */
  public EntityCloudCallback(
      @NotNull final MinecraftMediaLibrary library,
      final UUID[] viewers,
      @NotNull final Location location,
      final int width,
      final int height,
      final int videoWidth,
      final int delay) {
    this.library = library;
    this.viewers = viewers;
    this.location = location;
    entities = getCloudEntities();
    this.width = width;
    this.height = height;
    this.videoWidth = videoWidth;
    this.delay = delay;
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
   * Spawns the proper clouds at the location.
   *
   * @return the entities
   */
  private Entity[] getCloudEntities() {
    final int height = getHeight();
    final Entity[] ents = new Entity[height];
    final Location spawn = location.clone();
    final World world = spawn.getWorld();
    if (world != null) {
      for (int i = height - 1; i >= 0; i--) {
        final AreaEffectCloud cloud =
            (AreaEffectCloud) spawn.getWorld().spawnEntity(spawn, EntityType.AREA_EFFECT_CLOUD);
        ents[i] = cloud;
        cloud.setInvulnerable(true);
        cloud.setDuration(999999);
        cloud.setDurationOnUse(0);
        cloud.setRadiusOnUse(0);
        cloud.setRadius(0);
        cloud.setRadiusPerTick(0);
        cloud.setReapplicationDelay(0);
        cloud.setCustomNameVisible(true);
        cloud.setCustomName(StringUtils.repeat("-", height));
        cloud.setGravity(false);
        spawn.add(0, 0.225d, 0);
      }
    }
    return ents;
  }

  /**
   * Sends the necessary data onto the clouds while dithering.
   *
   * @param data to send
   */
  @Override
  public void send(final int[] data) {
    final long time = System.currentTimeMillis();
    if (time - lastUpdated >= delay) {
      lastUpdated = time;
      library.getHandler().displayEntities(viewers, entities, data, width);
    }
  }

  /**
   * Get viewers uuid [ ].
   *
   * @return the uuid [ ]
   */
  public UUID[] getViewers() {
    return viewers;
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
   * Gets height.
   *
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * Gets delay.
   *
   * @return the delay
   */
  public int getDelay() {
    return delay;
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
   * Gets video width.
   *
   * @return the video width
   */
  public int getVideoWidth() {
    return videoWidth;
  }

  /**
   * Gets last updated.
   *
   * @return the last updated
   */
  public long getLastUpdated() {
    return lastUpdated;
  }

  /**
   * Gets location.
   *
   * @return the location
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Gets the cloud entities.
   *
   * @return the entities
   */
  public Entity[] getEntities() {
    return entities;
  }

  /** The type Builder. */
  public static class Builder {

    private UUID[] viewers;
    private Location location;
    private int map;
    private int width;
    private int height;
    private int videoWidth;
    private int delay;

    private Builder() {}

    /**
     * Sets viewers.
     *
     * @param viewers the viewers
     * @return the viewers
     */
    public Builder setViewers(@NotNull final UUID[] viewers) {
      this.viewers = viewers;
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
     * Sets video width.
     *
     * @param videoWidth the video width
     * @return the video width
     */
    public Builder setVideoWidth(final int videoWidth) {
      this.videoWidth = videoWidth;
      return this;
    }

    /**
     * Sets delay.
     *
     * @param delay the delay
     * @return the delay
     */
    public Builder setDelay(final int delay) {
      this.delay = delay;
      return this;
    }

    /**
     * Sets the location.
     *
     * @param location the location
     * @return the location
     */
    public Builder setLocation(final Location location) {
      this.location = location;
      return this;
    }

    /**
     * Create entity cloud callback
     *
     * @param library the library
     * @return the entity cloud callback
     */
    public EntityCloudCallback build(final MinecraftMediaLibrary library) {
      return new EntityCloudCallback(library, viewers, location, width, height, videoWidth, delay);
    }
  }
}
