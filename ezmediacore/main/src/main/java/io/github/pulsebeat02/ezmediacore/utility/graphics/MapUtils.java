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
package io.github.pulsebeat02.ezmediacore.utility.graphics;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;


public final class MapUtils {

  private MapUtils() {}

  
  public static ItemStack getMapFromID(final int id) {

    final ItemStack map = new ItemStack(Material.FILLED_MAP);

    final MapMeta mapMeta = requireNonNull((MapMeta) map.getItemMeta());
    mapMeta.setMapId(id);
    map.setItemMeta(mapMeta);

    final ItemMeta itemMeta = map.getItemMeta();
    itemMeta.setLore(List.of("Map ID [%s]".formatted(id)));
    map.setItemMeta(itemMeta);

    return map;
  }

  public static void givePlayerMap( final Player player, final int id) {
    checkNotNull(player, "Player cannot be null!");
    player.getInventory().addItem(getMapFromID(id));
  }

  public static void buildMapScreen(
       final Player player,
       final Material mat,
      final int width,
      final int height,
      final int startingMap) {

    checkNotNull(player, "Player cannot be null!");
    checkNotNull(mat, "Material cannot be null!");

    final World world = player.getWorld();
    final BlockFace face = player.getFacing();
    final BlockFace opposite = face.getOppositeFace();
    final Block start = player.getLocation().getBlock().getRelative(face);

    // Start at top left corner
    int map = startingMap;
    for (int h = height; h > 0; h--) {
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
