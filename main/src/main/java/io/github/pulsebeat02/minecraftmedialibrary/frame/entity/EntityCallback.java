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

package io.github.pulsebeat02.minecraftmedialibrary.frame.entity;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * The callback used for clouds to update entity clouds for each frame when necessary.
 *
 * <p>When defining, you must pass in the basic arguments, but also a ScreenEntityType which defines
 * what type of Entity you are going to use for the callback. This entity could be any type of
 * entity.
 *
 * <p>For example, if you wanted to use the built in AreaEffectCloud (Cloud Particle) player, you
 * could pass in ScreenEntityType.AREA_EFFECT_CLOUD and it would automatically customize the entity
 * for you.
 *
 * <p>Or for an ArmorStand player, you could pass in ScreenEntityType.ARMORSTAND and it would
 * customize that specific entity accordingly.
 *
 * <p>For custom implementations, you would need to pass in ScreenEntityType.CUSTOM to tell the
 * player that you are using a custom entity for the player, AND override the
 * EntityCloudCallback#modifyEntity class to modify and return the entity properly. If you don't
 * override the method, it will throw many exceptions into the console explaining that the method
 * has to be overridden in order to function properly.
 *
 * <p>The ScreenEntityType and method override SHOULD ALWAYS BE SYNCED. For example, when you use
 * ScreenEntityType.AREA_EFFECT_CLOUD or ScreenEntityType.ARMORSTAND, you SHOULD NOT be overriding
 * the method as the library will be able to do that for you. You should only override it if you are
 * using ScreenEntityType.CUSTOM, which is where you manually customise it yourself.
 */
public final class EntityCallback implements EntityCallbackPrototype {

  private final PacketHandler handler;
  private final MediaLibrary library;
  private final UUID[] viewers;
  private final Location location;
  private final String charType;
  private final ScreenEntityType type;
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
   * @param type the screen type
   * @param width the width
   * @param height the height
   * @param videoWidth the video width
   * @param delay the delay
   */
  public EntityCallback(
      @NotNull final MediaLibrary library,
      final UUID[] viewers,
      @NotNull final Location location,
      @NotNull final ScreenEntityType type,
      final int width,
      final int height,
      final int videoWidth,
      final int delay) {
    handler = library.getHandler();
    this.library = library;
    this.viewers = viewers;
    this.location = location;
    this.type = type;
    charType = "-";
    this.width = width;
    this.height = height;
    this.videoWidth = videoWidth;
    this.delay = delay;
    entities = getCloudEntities();
  }

  /**
   * Instantiates a new EntityCloudCallback.
   *
   * @param library the library
   * @param viewers the viewers
   * @param location the location
   * @param str the String used for the name
   * @param type the screen type
   * @param width the width
   * @param height the height
   * @param videoWidth the video width
   * @param delay the delay
   */
  public EntityCallback(
      @NotNull final MediaLibrary library,
      final UUID[] viewers,
      @NotNull final Location location,
      @NotNull final String str,
      @NotNull final ScreenEntityType type,
      final int width,
      final int height,
      final int videoWidth,
      final int delay) {
    handler = library.getHandler();
    this.viewers = viewers;
    this.location = location;
    this.type = type;
    this.library = library;
    charType = str;
    this.width = width;
    this.height = height;
    this.videoWidth = videoWidth;
    this.delay = delay;
    entities = getCloudEntities();
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
    final EntityType entityType =
        type == ScreenEntityType.AREA_EFFECT_CLOUD
            ? EntityType.AREA_EFFECT_CLOUD
            : type == ScreenEntityType.ARMORSTAND
                ? EntityType.ARMOR_STAND
                : EntityType.AREA_EFFECT_CLOUD;
    if (world != null) {
      for (int i = height - 1; i >= 0; i--) {
        ents[i] = modifyEntity(world.spawnEntity(spawn, entityType));
        spawn.add(0, 0.225d, 0);
      }
    }
    return ents;
  }

  @Override
  public Entity modifyEntity(@NotNull final Entity entity) {
    switch (type) {
      case AREA_EFFECT_CLOUD:
        return EntityModificationUtilities.getModifiedAreaEffectCloud(
            (AreaEffectCloud) entity, charType, getHeight());
      case ARMORSTAND:
        return EntityModificationUtilities.getModifiedArmorStand(
            (ArmorStand) entity, charType, getHeight());
      case CUSTOM:
        /*
        Override the method for custom entity
         */
        break;
    }
    throw new IllegalArgumentException("Custom entity must have the modifyEntity overridden!");
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
      handler.displayEntities(viewers, entities, data, width);
    }
  }

  @Override
  public UUID[] getViewers() {
    return viewers;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public int getDelay() {
    return delay;
  }

  @Override
  public MediaLibrary getLibrary() {
    return library;
  }

  @Override
  public PacketHandler getHandler() {
    return handler;
  }

  @Override
  public int getVideoWidth() {
    return videoWidth;
  }

  @Override
  public long getLastUpdated() {
    return lastUpdated;
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public Entity[] getEntities() {
    return entities;
  }

  @Override
  public String getCharType() {
    return charType;
  }

  @Override
  public ScreenEntityType getType() {
    return type;
  }

  /** The type Builder. */
  public static class Builder {

    private UUID[] viewers;
    private Location location;
    private ScreenEntityType type;
    private int width;
    private int height;
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
    public Builder setEntityWidth(final int width) {
      this.width = width;
      return this;
    }

    /**
     * Sets height.
     *
     * @param height the height
     * @return the height
     */
    public Builder setEntityHeight(final int height) {
      this.height = height;
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
     * Sets the proper entity type of the player.
     *
     * @param type the entity type
     * @return the type
     */
    public Builder setType(final ScreenEntityType type) {
      this.type = type;
      return this;
    }

    /**
     * Create entity cloud callback
     *
     * @param library the library
     * @return the entity cloud callback
     */
    public EntityCallbackPrototype build(final MediaLibrary library) {
      return new EntityCallback(library, viewers, location, type, width, height, width, delay);
    }
  }
}
