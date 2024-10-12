/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.callback.video;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.entity.EntityCallbackDispatcher;
import io.github.pulsebeat02.ezmediacore.callback.entity.NamedStringCharacter;
import rewrite.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.utility.structure.Pair;
import java.nio.IntBuffer;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Contract;



public class EntityCallback<T extends Entity> extends FrameCallback
    implements EntityCallbackDispatcher {

  private final Class<T> type;
  private final Consumer<T> consumer;
  private final Entity[] entities;
  private final Location location;
  private final NamedStringCharacter name;

  EntityCallback(
       final EzMediaCore core,
       final Viewers viewers,
       final Dimension dimension,
       final Location location,
       final NamedStringCharacter character,
       final Class<T> type,
       final Consumer<T> consumer,
       final DelayConfiguration delay) {
    super(core, viewers, dimension, delay);
    checkNotNull(location, "Location cannot be null!");
    checkNotNull(type, "Entity class cannot be null!");
    checkNotNull(character, "Name of entity cannot be null!");
    this.location = location;
    this.type = type;
    this.consumer = consumer;
    this.name = character;
    this.entities = this.getModifiedEntities();
  }

  private Entity  [] getModifiedEntities() {
    final int height = this.getDimensions().getHeight();
    final Entity[] entities = new Entity[height];
    this.spawnEntity(entities, this.location.clone(), this.location.getWorld(), height);
    return entities;
  }

  private void spawnEntity(
       final Entity  [] entities,
       final Location spawn,
       final World world,
      final int height) {
    for (int i = height - 1; i >= 0; i--) {
      entities[i] =
          world.spawn(spawn, this.type, this.consumer == null ? null : this.consumer::accept);
      entities[i].setCustomName(this.repeat(this.name.getCharacter(), height));
      entities[i].setCustomNameVisible(true);
      spawn.add(0.0, 0.225, 0.0);
    }
  }

  private  String repeat( final String name, final int height) {
    return name.repeat(Math.max(0, height));
  }

  @Override
  public void preparePlayerStateChange(
       final VideoPlayer player,  final PlayerControls status) {
    super.preparePlayerStateChange(player, status);
    if (status == PlayerControls.RELEASE) {
      this.removeEntities();
    }
  }

  private void removeEntities() {
    if (this.entities != null) {
      for (final Entity entity : this.entities) {
        entity.remove();
      }
    }
  }

  @Override
  public void process(final int  [] data) {
    final long time = System.currentTimeMillis();
    final int width = this.getDimensions().getWidth();
    final UUID[] viewers = this.getWatchers().getViewers();
    final String name = this.name.getCharacter();
    if (time - this.getLastUpdated() >= this.getDelayConfiguration().getDelay()) {
      this.setLastUpdated(time);
      this.displayEntity(viewers, name, IntBuffer.wrap(data), width);
    }
  }

  private void displayEntity(
       final UUID[] viewers,
       final String name,
       final IntBuffer data,
      final int width) {
    final UUID[] watchers = this.getWatchers().getViewers();
    final int height = this.entities.length;
    this.getPacketHandler().displayEntities(watchers, this.entities, data, name, width, height);
  }

  @Override
  public  Entity[] getEntities() {
    return this.entities;
  }

  @Override
  public  NamedStringCharacter getStringName() {
    return this.name;
  }

  @Override
  public  Location getLocation() {
    return this.location;
  }

  @SuppressWarnings("unchecked")
  public static final class Builder<T extends Entity> extends VideoCallbackBuilder {

    private static final Pair<EntityType, Consumer<AreaEffectCloud>> AREA_EFFECT_CLOUD;
    private static final Pair<EntityType, Consumer<ArmorStand>> ARMOR_STAND;

    static {
      AREA_EFFECT_CLOUD = Pair.ofPair(EntityType.AREA_EFFECT_CLOUD, getAreaEffectCloudConsumer());
      ARMOR_STAND = Pair.ofPair(EntityType.ARMOR_STAND, getArmorStandConsumer());
    }

    private NamedStringCharacter character = NamedStringCharacter.NORMAL_SQUARE;
    private Location location;
    private Class<T> entity;
    private Consumer<T> consumer;

    public Builder() {}

    private static  Consumer<AreaEffectCloud> getAreaEffectCloudConsumer() {
      return entity -> {
        entity.setInvulnerable(true);
        entity.setDuration(999999);
        entity.setDurationOnUse(0);
        entity.setRadiusOnUse(0);
        entity.setRadius(0);
        entity.setRadiusPerTick(0);
        entity.setReapplicationDelay(0);
        entity.setGravity(false);
      };
    }

    private static  Consumer<ArmorStand> getArmorStandConsumer() {
      return entity -> {
        entity.setInvulnerable(true);
        entity.setVisible(false);
        entity.setGravity(false);
      };
    }

    @Contract("_ -> this")
    public  Builder<T> location( final Location location) {
      this.location = location;
      return this;
    }

    @Contract("_ -> this")
    public  Builder<T> character( final NamedStringCharacter character) {
      this.character = character;
      return this;
    }

    @Contract("_ -> this")
    public  Builder<T> entityType( final Class<T> entity) {
      this.entity = entity;
      return this;
    }

    @Contract("_ -> this")
    public  Builder<T> consumer( final Consumer<T> consumer) {
      this.consumer = consumer;
      return this;
    }

    @Contract(" -> this")
    public  Builder<T> areaEffectCloudPlayer() {
      this.entity = (Class<T>) AREA_EFFECT_CLOUD.getKey().getEntityClass();
      this.consumer = (Consumer<T>) AREA_EFFECT_CLOUD.getValue();
      return this;
    }

    @Contract(" -> this")
    public  Builder<T> armorStandPlayer() {
      this.entity = (Class<T>) ARMOR_STAND.getKey().getEntityClass();
      this.consumer = (Consumer<T>) ARMOR_STAND.getValue();
      return this;
    }

    @Contract("_ -> new")
    @Override
    public  FrameCallback build( final EzMediaCore core) {
      checkNotNull(this.location, "Location cannot be null!");
      checkNotNull(this.entity, "Entity cannot be null!");
      checkNotNull(this.consumer, "Consumer (for entity) cannot be null!");
      return new EntityCallback<>(
          core,
          this.getViewers(),
          this.getDims(),
          this.location,
          this.character,
          this.entity,
          this.consumer,
          this.getDelay());
    }
  }
}
