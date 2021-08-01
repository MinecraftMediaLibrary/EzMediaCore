package io.github.pulsebeat02.ezmediacore.listener;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.jetbrains.annotations.NotNull;

public final class MapInteractionListener implements Listener {

  private final MediaLibraryCore core;

  public MapInteractionListener(@NotNull final MediaLibraryCore core) {
    this.core = core;
  }

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
    if (core.getHandler()
        .isMapRegistered(((MapMeta) Objects.requireNonNull(stack.getItemMeta())).getMapId())) {
      event.setCancelled(true);
    }
  }
}
