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
package rewrite.util.bukkit;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;

public final class MapUtils {

  private MapUtils() {
    throw new UnsupportedOperationException();
  }

  public static ItemStack generateMapItemStack(final int id) {
    final String line = "ID [%s]".formatted(id);
    final List<String> lore = List.of(line);
    final ItemStack map = new ItemStack(Material.FILLED_MAP);
    final ItemMeta meta = requireNonNull(map.getItemMeta());
    if (meta instanceof final MapMeta mapMeta) {
      mapMeta.setLore(lore);
      mapMeta.setMapId(id);
      map.setItemMeta(mapMeta);
    }
    return map;
  }

  public static void givePlayerMap( final Player player, final int id) {
    final ItemStack map = generateMapItemStack(id);
    final PlayerInventory inventory = player.getInventory();
    inventory.addItem(map);
  }

  public static void createVideoScreen(
       final Player player,
       final Material mat,
      final int width,
      final int height,
      final int startingMap) {
    final World world = player.getWorld();
    final BlockFace face = player.getFacing();
    final BlockFace opposite = face.getOppositeFace();
    final Location playerLocation = player.getLocation();
    final Block block = playerLocation.getBlock();
    final Block start = block.getRelative(face);
    int map = startingMap;
    for (int h = height; h > 0; h--) {
      for (int w = 0; w < width; w++, map++) {
        final Block up = start.getRelative(BlockFace.UP, h);
        final Block east = up.getRelative(BlockFace.EAST, w);
        final Block relative = east.getRelative(opposite);
        final Location location = relative.getLocation();
        final ItemStack stack = generateMapItemStack(map);
        final ItemFrame frame = world.spawn(location, ItemFrame.class);
        frame.setFacingDirection(face);
        frame.setItem(stack);
        east.setType(mat);
      }
    }
  }
}
