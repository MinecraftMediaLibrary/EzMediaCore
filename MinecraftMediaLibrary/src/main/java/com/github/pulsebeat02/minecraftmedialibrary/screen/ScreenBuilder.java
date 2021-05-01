package com.github.pulsebeat02.minecraftmedialibrary.screen;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ScreenBuilder {

  private final Location location;

  public ScreenBuilder(@NotNull final Player player) {
    location = player.getLocation();
  }

  /*

  PLAYER LOCATION
  |
  |
  X X X X X ... width
  X X X X X ...
  X X X X X ...
  X X X X X ...
  X X X X X ...
  ... ... ...
  height

  AREA = width * height

  for slot in area
      row = slot divide width
      col = slot % width
      newX = playerLocX +

   */
  public void buildMapScreen(
      @NotNull final Location loc,
      @NotNull final Material mat,
      final int width,
      final int height,
      final int startingMap) {
    final int maps = width * height;
    final World world = Objects.requireNonNull(loc.getWorld());
    for (int i = 0; i < maps; i++) {
      final int row = i / width;
      final int col = i % width;
      world.getBlockAt(loc.getBlockX() + col, loc.getBlockY() + row, loc.getBlockZ()).setType(mat);
      // ...
    }

  }
}
