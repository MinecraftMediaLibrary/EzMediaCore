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
package io.github.pulsebeat02.ezmediacore.listener;

import static java.util.Objects.requireNonNull;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import org.bukkit.Material;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;


public record MapInteractionListener(
    EzMediaCore core) implements
    Listener {

  public MapInteractionListener( final EzMediaCore core) {
    this.core = core;
  }

  @EventHandler
  public void onPlayerInteract( final HangingBreakByEntityEvent event) {

    final Hanging hanging = event.getEntity();
    if (!this.isItemFrame(hanging)) {
      return;
    }

    final ItemStack stack = ((ItemFrame) hanging).getItem();
    if (!this.isFilledMap(stack)) {
      return;
    }

    this.correctInteraction(event, stack);
  }

  private boolean isFilledMap( final ItemStack stack) {
    return stack.getType() == Material.FILLED_MAP;
  }

  private boolean isItemFrame( final Hanging hanging) {
    return hanging instanceof ItemFrame;
  }

  private void correctInteraction( final HangingBreakByEntityEvent event,
       final ItemStack stack) {
    if (this.isMapRegistered(stack)) {
      event.setCancelled(true);
    }
  }

  private boolean isMapRegistered( final ItemStack stack) {
    return this.core
        .getHandler()
        .isMapRegistered(this.getMapID(stack));
  }

  private int getMapID( final ItemStack stack) {
    //noinspection deprecation
    return ((MapMeta) requireNonNull(stack.getItemMeta())).getMapId();
  }

  @Override
  public
  EzMediaCore getCore() {
    return this.core;
  }
}
