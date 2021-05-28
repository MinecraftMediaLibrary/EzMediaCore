/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.screen;

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

/** Screen builder class used to assist building screens with maps. */
public class ScreenBuilder {

  private final Location location;

  /**
   * Instantiates a new ScreenBuilder.
   *
   * @param player the player
   */
  public ScreenBuilder(@NotNull final Player player) {
    location = player.getLocation();
  }

  /**
   * Builds a new screen based on the player location.
   *
   * @param mat the material to use
   * @param width the width of the screen in blocks
   * @param height the height of the screen in blocks
   * @param startingMap the map to use starting off with
   */
  public void buildMapScreen(
      @NotNull final Material mat, final int width, final int height, final int startingMap) {

    final Vector direction = location.getDirection();
    final Location center = location.clone().add(direction);

    final Location rotate = location.clone();
    rotate.setPitch(0);
    rotate.setYaw(location.getYaw() - 90);
    final Vector rotation = rotate.getDirection();

    final Location blockLocation = center.clone().add(rotate).subtract(0, width / 2f, 0);
    rotate.multiply(-1);

    final int initialX = blockLocation.getBlockX();
    final int initialZ = blockLocation.getBlockZ();
    final World world = Objects.requireNonNull(location.getWorld());
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
