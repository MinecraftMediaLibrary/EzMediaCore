package io.github.pulsebeat02.epicmedialib.utility;

import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.jetbrains.annotations.NotNull;

public final class MapUtils {

  private MapUtils() {}

  @NotNull
  public static ItemStack getMapFromID(final int id) {
    final ItemStack map = new ItemStack(Material.FILLED_MAP);
    final MapMeta meta = Objects.requireNonNull((MapMeta) map.getItemMeta());
    meta.setMapId(id);
    map.setItemMeta(meta);
    return map;
  }

  public static void givePlayerMap(@NotNull final Player player, final int id) {
    player.getInventory().addItem(getMapFromID(id));
  }

  public static void buildMapScreen(
      @NotNull final Player player,
      @NotNull final Material mat,
      final int width,
      final int height,
      final int startingMap) {

    final World world = player.getWorld();
    final BlockFace face = player.getFacing();
    final BlockFace opposite = face.getOppositeFace();
    final Block start = player.getLocation().getBlock().getRelative(face);

    // Start at top left corner
    int map = 0;
    for (int h = height; h >= 0; h--) {
      for (int w = 0; w < width; w++) {
        final Block current = start.getRelative(BlockFace.UP, h).getRelative(BlockFace.EAST, w);
        current.setType(mat);

        final ItemFrame frame =
            world.spawn(current.getRelative(opposite).getLocation(), ItemFrame.class);
        frame.setFacingDirection(face);
        frame.setItem(getMapFromID(map));

        map++;
      }
    }
  }
}
