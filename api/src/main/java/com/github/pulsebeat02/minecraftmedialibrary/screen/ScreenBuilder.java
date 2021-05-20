package com.github.pulsebeat02.minecraftmedialibrary.screen;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ScreenBuilder {

  private final Location location;

  public ScreenBuilder(@NotNull final Player player) {
    location = player.getLocation();
  }

  public void buildMapScreen(
      @NotNull final Location loc,
      @NotNull final Material mat,
      final int width,
      final int height,
      final int startingMap) {

    final Vector direction = loc.getDirection();
    final Location center = loc.clone().add(direction);

    final Location rotate = loc.clone();
    rotate.setPitch(0);
    rotate.setYaw(loc.getYaw() - 90);
    final Vector rotation = rotate.getDirection();

    final Location blockLocation = center.clone().add(rotate).subtract(0, width / 2f, 0);
    rotate.multiply(-1);

    final int initialX = blockLocation.getBlockX();
    final int initialZ = blockLocation.getBlockZ();
    final World world = Objects.requireNonNull(loc.getWorld());
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        final int map = startingMap + x * y;
        final Block block = blockLocation.getBlock();
        block.setType(mat);
      }
      blockLocation.add(0, 1, 0);
      blockLocation.setX(initialX);
      blockLocation.setZ(initialZ);
    }
  }

  public ItemStack getMapFromID(final int id) {
    final ItemStack map = new ItemStack(Material.FILLED_MAP);
    final MapMeta meta = (MapMeta) map.getItemMeta();
    meta.setMapId(id);
    map.setItemMeta(meta);
    return map;
  }
}
