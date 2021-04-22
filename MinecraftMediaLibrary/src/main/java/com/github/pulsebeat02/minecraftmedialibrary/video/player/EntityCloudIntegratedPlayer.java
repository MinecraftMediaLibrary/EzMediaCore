package com.github.pulsebeat02.minecraftmedialibrary.video.player;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * A VLCJ integrated player used to play videos in Minecraft. The library uses a callback for the
 * specific function from native libraries. It renders it on entities.
 */
public class EntityCloudIntegratedPlayer extends VideoPlayer {

  private final Location location;
  private Entity[] entities;

  /**
   * Instantiates a new Vlcj integrated player on entity clouds.
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
      @NotNull final Consumer<int[]> callback,
      @NotNull final Location location,
      final int width,
      final int height) {
    super(library, url, width, height, callback);
    this.location = location;
    Logger.info(String.format("Created a VLCJ Integrated Entity Cloud Video Player (%s)", url));
  }

  /**
   * Instantiates a new VLCJIntegratedPlayer.
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
      @NotNull final File file,
      @NotNull final Consumer<int[]> callback,
      @NotNull final Location location,
      final int width,
      final int height) {
    super(library, file, width, height, callback);
    this.location = location;
    Logger.info(
        String.format("Created a VLCJ Integrated Video Player (%s)", file.getAbsolutePath()));
  }

  /**
   * Spawns the proper clouds at the location.
   *
   * @return the entities
   */
  public Entity[] getCloudEntities() {
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
    entities = getCloudEntities();
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
    private int width;
    private int height;
    private Consumer<int[]> callback;
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

    public Builder setCallback(final Consumer<int[]> callback) {
      this.callback = callback;
      return this;
    }

    public Builder setLocation(final Location location) {
      this.location = location;
      return this;
    }

    public EntityCloudIntegratedPlayer build(
        @NotNull final MinecraftMediaLibrary library) {
      return new EntityCloudIntegratedPlayer(library, url, callback, location, width, height);
    }
  }
}
