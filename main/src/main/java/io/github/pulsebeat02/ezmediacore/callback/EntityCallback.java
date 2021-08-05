package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.utility.ImmutableDimension;
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
      @NotNull final ImmutableDimension dimension,
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
        case AREA_EFFECT_CLOUD:
          for (int i = height - 1; i >= 0; i--) {
            ents[i] = this.getAreaEffectCloud(world, spawn, height);
            spawn.add(0.0, 0.225, 0.0);
          }
          break;
        case ARMORSTAND:
          for (int i = height - 1; i >= 0; i--) {
            ents[i] = this.getArmorStand(world, spawn, height);
            spawn.add(0.0, 0.225, 0.0);
          }
          break;
        case CUSTOM:
          final Consumer<ArmorStand> consumer = this.modifyEntity();
          if (consumer == null) {
            throw new AssertionError("Must override the modifyEntity method for custom entity!");
          }
          for (int i = height - 1; i >= 0; i--) {
            final ArmorStand armorstand = world.spawn(spawn, ArmorStand.class, consumer::accept);
            ents[i] = armorstand;
            spawn.add(0.0, 0.225, 0.0);
          }
          break;
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
    switch (status) {
      case START, RELEASE -> this.removeEntities();
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
  public void process(final int[] data) {}

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
