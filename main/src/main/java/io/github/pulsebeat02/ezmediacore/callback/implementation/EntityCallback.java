/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.callback.implementation;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.CallbackBuilder;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.FrameCallback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.entity.EntityCallbackDispatcher;
import io.github.pulsebeat02.ezmediacore.callback.entity.NamedEntityString;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import java.util.function.Consumer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityCallback<T extends Entity> extends FrameCallback implements EntityCallbackDispatcher {

  private final Class<T> type;
  private final Consumer<T> consumer;
  private final Entity[] entities;
  private final Location location;
  private final NamedEntityString name;

  EntityCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers,
      @NotNull final Dimension dimension,
      @NotNull final Location location,
      @NotNull final NamedEntityString character,
      @NotNull final Class<T> type,
      @Nullable final Consumer<T> consumer,
      @NotNull final DelayConfiguration delay) {
    super(core, viewers, dimension, delay);
    this.location = location;
    this.type = type;
    this.consumer = consumer;
    this.name = character;
    this.entities = this.getModifiedEntities();
  }

  private Entity @NotNull [] getModifiedEntities() {
    final int height = this.getDimensions().getHeight();
    final Entity[] ents = new Entity[height];
    final Location spawn = this.location.clone();
    final World world = spawn.getWorld();
    if (world != null) {
      for (int i = height - 1; i >= 0; i--) {
        ents[i] =
            world.spawn(spawn, this.type, this.consumer == null ? null : this.consumer::accept);
        ents[i].setCustomName(StringUtils.repeat(this.name.getName(), height));
        ents[i].setCustomNameVisible(true);
        spawn.add(0.0, 0.225, 0.0);
      }
    }
    return ents;
  }

  @Override
  public void preparePlayerStateChange(@NotNull final PlayerControls status) {
    super.preparePlayerStateChange(status);
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
  public void process(final int[] data) {
    final long time = System.currentTimeMillis();
    final int width = this.getDimensions().getWidth();
    if (time - this.getLastUpdated() >= this.getDelayConfiguration().getDelay()) {
      this.setLastUpdated(time);
      this.getPacketHandler()
          .displayEntities(
              this.getWatchers().getViewers(),
              this.entities,
              data,
              width,
              this.entities.length / width);
    }
  }

  @Override
  public @NotNull Entity[] getEntities() {
    return this.entities;
  }

  @Override
  public @NotNull NamedEntityString getStringName() {
    return this.name;
  }

  @Override
  public @NotNull Location getLocation() {
    return this.location;
  }

  @SuppressWarnings("unchecked")
  public static final class Builder<T extends Entity> extends CallbackBuilder {

    private static final Pair<EntityType, Consumer<AreaEffectCloud>> AREA_EFFECT_CLOUD;
    private static final Pair<EntityType, Consumer<ArmorStand>> ARMOR_STAND;

    static {
      AREA_EFFECT_CLOUD =
          Pair.ofPair(
              EntityType.AREA_EFFECT_CLOUD,
              entity -> {
                entity.setInvulnerable(true);
                entity.setDuration(999999);
                entity.setDurationOnUse(0);
                entity.setRadiusOnUse(0);
                entity.setRadius(0);
                entity.setRadiusPerTick(0);
                entity.setReapplicationDelay(0);
                entity.setGravity(false);
              });
      ARMOR_STAND =
          Pair.ofPair(
              EntityType.ARMOR_STAND,
              entity -> {
                entity.setInvulnerable(true);
                entity.setVisible(false);
                entity.setGravity(false);
              });
    }

    private NamedEntityString character = NamedEntityString.NORMAL_SQUARE;
    private Location location;
    private Class<T> entity;
    private Consumer<T> consumer;

    public Builder() {}

    @Contract("_ -> this")
    @Override
    public @NotNull Builder<T> delay(@NotNull final DelayConfiguration delay) {
      super.delay(delay);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public @NotNull Builder<T> dims(@NotNull final Dimension dims) {
      super.dims(dims);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public @NotNull Builder<T> viewers(@NotNull final Viewers viewers) {
      super.viewers(viewers);
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder<T> location(@NotNull final Location location) {
      this.location = location;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder<T> character(@NotNull final NamedEntityString character) {
      this.character = character;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder<T> entityType(@NotNull final Class<T> entity) {
      this.entity = entity;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder<T> consumer(@NotNull final Consumer<T> consumer) {
      this.consumer = consumer;
      return this;
    }

    @Contract(" -> this")
    public @NotNull Builder<T> areaEffectCloudPlayer() {
      this.entity = (Class<T>) AREA_EFFECT_CLOUD.getKey().getEntityClass();
      this.consumer = (Consumer<T>) AREA_EFFECT_CLOUD.getValue();
      return this;
    }

    @Contract(" -> this")
    public @NotNull Builder<T> armorStandPlayer() {
      this.entity = (Class<T>) ARMOR_STAND.getKey().getEntityClass();
      this.consumer = (Consumer<T>) ARMOR_STAND.getValue();
      return this;
    }

    @Override
    public @NotNull FrameCallback build(@NotNull final MediaLibraryCore core) {
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
