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
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import java.util.Collection;
import java.util.function.Consumer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityCallback extends FrameCallback implements EntityCallbackDispatcher {

  private final EntityType type;
  private final Entity[] entities;
  private final Location location;
  private final String name;

  public EntityCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Dimension dimension,
      @NotNull final Collection<? extends Player> viewers,
      @NotNull final Location location,
      @NotNull final String character,
      @NotNull final EntityType type,
      final int blockWidth,
      final int delay) {
    super(core, dimension, viewers, blockWidth, delay);
    this.location = location;
    this.type = type;
    this.name = character;
    this.entities = this.getModifiedEntities();
  }

  private Entity[] getModifiedEntities() {
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

  private Entity getAreaEffectCloud(final World world, final Location location, final int height) {
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
          entity.setCustomName(StringUtils.repeat(this.name, height));
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
          entity.setCustomName(StringUtils.repeat(this.name, height));
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
    if (time - this.getLastUpdated() >= this.getFrameDelay()) {
      this.setLastUpdated(time);
      this.getPacketHandler()
          .displayEntities(this.getViewers(), this.entities, data, this.getDimensions().getWidth());
    }
  }

  @Override
  public @NotNull Entity[] getEntities() {
    return this.entities;
  }

  @Override
  public @NotNull String getStringName() {
    return this.name;
  }

  @Override
  public @NotNull Location getLocation() {
    return this.location;
  }
}
