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

package io.github.pulsebeat02.minecraftmedialibrary.listener;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import org.bukkit.Material;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Provider class which ignores interaction on itemframes. Can be used by plugins if they want. (Not
 * used by default)
 */
public class PlayerInteractionHandler implements Listener {

  private final MediaLibrary library;

  /**
   * Instantiates a new PlayerInteractionHandler.
   *
   * @param library the library
   */
  public PlayerInteractionHandler(@NotNull final MediaLibrary library) {
    this.library = library;
  }

  /**
   * Checks if the map id is registered and used as a video or not.
   *
   * @param event the event
   */
  @EventHandler
  public void onPlayerInteract(final HangingBreakByEntityEvent event) {
    final Hanging hanging = event.getEntity();
    if (!(hanging instanceof ItemFrame)) {
      return;
    }
    final ItemStack stack = ((ItemFrame) hanging).getItem();
    if (stack.getType() != Material.FILLED_MAP) {
      return;
    }
    if (library
        .getHandler()
        .isMapRegistered(((MapMeta) Objects.requireNonNull(stack.getItemMeta())).getMapId())) {
      event.setCancelled(true);
    }
  }
}
