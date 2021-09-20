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
package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.entity.EntityCallbackDispatcher;
import io.github.pulsebeat02.ezmediacore.callback.entity.NamedEntityString;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import java.util.function.Consumer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityCallback extends FrameCallback implements EntityCallbackDispatcher {

  private final EntityType type;
  private final Entity[] entities;
  private final Location location;
  private final NamedEntityString name;

  EntityCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers,
      @NotNull final Dimension dimension,
      @NotNull final Location location,
      @NotNull final NamedEntityString character,
      @NotNull final EntityType type,
      @NotNull final DelayConfiguration delay) {
    super(core, viewers, dimension, delay);
    this.location = location;
    this.type = type;
    this.name = character;
    this.entities = this.getModifiedEntities();
  }

  private Entity @NotNull [] getModifiedEntities() {
    final int height = this.getDimensions().getHeight();
    final Entity[] ents = new Entity[height];
    final Location spawn = this.location.clone();
    final World world = spawn.getWorld();
    if (world != null) {
      switch (this.type) {
        case AREA_EFFECT_CLOUD -> {
          for (int i = height - 1; i >= 0; i--) {
            ents[i] = this.getAreaEffectCloud(world, spawn, height);
            spawn.add(0.0, 0.225, 0.0);
          }
        }
        case ARMORSTAND -> {
          for (int i = height - 1; i >= 0; i--) {
            ents[i] = this.getArmorStand(world, spawn, height);
            spawn.add(0.0, 0.225, 0.0);
          }
        }
        case CUSTOM -> {
          final Consumer<ArmorStand> consumer = this.modifyEntity();
          if (consumer == null) {
            throw new AssertionError("Must override the modifyEntity method for custom entity!");
          }
          for (int i = height - 1; i >= 0; i--) {
            final ArmorStand armorstand = world.spawn(spawn, ArmorStand.class, consumer::accept);
            ents[i] = armorstand;
            spawn.add(0.0, 0.225, 0.0);
          }
        }
        default -> throw new IllegalArgumentException("Entity type not valid!");
      }
    }
    return ents;
  }

  private @NotNull Entity getAreaEffectCloud(@NotNull final World world, final Location location,
      final int height) {
    return world.spawn(
        location,
        AreaEffectCloud.class,
        entity -> {
          entity.setInvulnerable(true);
          entity.setDuration(999999);
          entity.setDurationOnUse(0);
          entity.setRadiusOnUse(0);
          entity.setRadius(0);
          entity.setRadiusPerTick(0);
          entity.setReapplicationDelay(0);
          entity.setCustomNameVisible(true);
          entity.setCustomName(StringUtils.repeat(this.name.getName(), height));
          entity.setGravity(false);
        });
  }

  private Entity getArmorStand(final World world, final Location location, final int height) {
    return world.spawn(
        location,
        ArmorStand.class,
        entity -> {
          entity.setInvulnerable(true);
          entity.setVisible(false);
          entity.setCustomNameVisible(true);
          entity.setGravity(false);
          entity.setCustomName(StringUtils.repeat(this.name.getName(), height));
        });
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
  public @Nullable <T> Consumer<T> modifyEntity() {
    return null;
  }

  @Override
  public void process(final int[] data) {
    final long time = System.currentTimeMillis();
    if (time - this.getLastUpdated() >= this.getDelayConfiguration().getDelay()) {
      this.setLastUpdated(time);
      this.getPacketHandler()
          .displayEntities(this.getWatchers().getViewers(), this.entities, data, this.getDimensions().getWidth());
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

  public static final class Builder extends CallbackBuilder {

    private NamedEntityString character = NamedEntityString.NORMAL_SQUARE;
    private Location location;
    private EntityType type = EntityType.ARMORSTAND;
    Builder() {
    }

    @Contract("_ -> this")
    @Override
    public @NotNull Builder delay(@NotNull final DelayConfiguration delay) {
      super.delay(delay);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public @NotNull Builder dims(@NotNull final Dimension dims) {
      super.dims(dims);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public @NotNull Builder viewers(@NotNull final Viewers viewers) {
      super.viewers(viewers);
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder location(@NotNull final Location location) {
      this.location = location;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder character(@NotNull final NamedEntityString character) {
      this.character = character;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder type(@NotNull final EntityType type) {
      this.type = type;
      return this;
    }

    @Override
    public @NotNull FrameCallback build(@NotNull final MediaLibraryCore core) {
      return new EntityCallback(
          core,
          this.getViewers(),
          this.getDims(),
          this.location,
          this.character,
          this.type,
          this.getDelay());
    }
  }
}
