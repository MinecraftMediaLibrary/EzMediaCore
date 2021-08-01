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

package io.github.pulsebeat02.deluxemediaplugin.command.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.minecraftmedialibrary.utility.MapUtilities;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class ScreenBuilderGui {

  private final Gui gui;
  private final Player viewer;
  private final DeluxeMediaPlugin plugin;
  private Material material;
  private int width;
  private int height;
  private int id;

  public ScreenBuilderGui(@NotNull final DeluxeMediaPlugin pl, @NotNull final Player player) {
    gui = Gui.gui().rows(6).title(text("Choose Screen Size", GOLD)).create();
    plugin = pl;
    viewer = player;
    width = 5;
    height = 5;
    id = 0;
    CompletableFuture.runAsync(this::initialize);
    gui.open(player);
  }

  private void initialize() {

    gui.setDefaultClickAction(x -> x.setCancelled(true));

    final GuiItem increaseWidth = new GuiItem(getIncreaseArrow("Width"));
    increaseWidth.setAction(
        x -> {
          width++;
          update();
        });

    final GuiItem decreaseWidth = new GuiItem(getDecreaseArrow("Width"));
    increaseWidth.setAction(
        x -> {
          width--;
          update();
        });

    final GuiItem increaseHeight = new GuiItem(getIncreaseArrow("Height"));
    increaseHeight.setAction(
        x -> {
          height++;
          update();
        });

    final GuiItem decreaseHeight = new GuiItem(getDecreaseArrow("Height"));
    decreaseHeight.setAction(
        x -> {
          height--;
          update();
        });

    final GuiItem increaseID = new GuiItem(getIncreaseArrow("Map ID"));
    increaseID.setAction(
        x -> {
          id++;
          update();
        });

    final GuiItem decreaseID = new GuiItem(getDecreaseArrow("Map ID"));
    decreaseID.setAction(
        x -> {
          id--;
          update();
        });

    final GuiItem buildScreen =
        ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE)
            .name(text("Build Screen", GREEN))
            .asGuiItem();
    buildScreen.setAction(
        x -> {
          viewer.closeInventory();
          MapUtilities.buildMapScreen(viewer, material, width, height, id);
          plugin
              .audience()
              .sender(viewer)
              .sendMessage(format(text("Successfully built your new screen!", GREEN)));
          viewer.playSound(viewer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
        });
    gui.setItem(50, buildScreen);

    gui.setItem(12, increaseWidth);
    gui.setItem(30, decreaseWidth);

    gui.setItem(14, increaseHeight);
    gui.setItem(32, decreaseHeight);

    gui.setItem(16, increaseID);
    gui.setItem(34, decreaseID);

    update();
  }

  private void update() {

    final GuiItem materialItem =
        ItemBuilder.from(material)
            .name(
                TextComponent.ofChildren(
                    text("Material - ", GOLD), text(material.toString(), AQUA)))
            .asGuiItem();
    materialItem.setAction(
        x -> {
          final ItemStack stack = x.getCursor();
          if (stack == null) {
            return;
          }
          material = stack.getType();
          materialItem.setItemStack(
              ItemBuilder.from(material)
                  .name(
                      TextComponent.ofChildren(
                          text("Material - ", GOLD), text(material.toString(), AQUA)))
                  .build());
        });
    gui.setItem(19, materialItem);

    final GuiItem widthItem =
        ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(text(String.format("Screen Width (%d Blocks)", width), GOLD))
            .asGuiItem();
    gui.setItem(22, widthItem);

    final GuiItem heightItem =
        ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(text(String.format("Screen Height (%d Blocks)", height), GOLD))
            .asGuiItem();
    gui.setItem(24, heightItem);

    final GuiItem idItem =
        ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(text(String.format("Starting Map ID (%d)", id), GOLD))
            .asGuiItem();
    gui.setItem(26, idItem);
  }

  private ItemStack getIncreaseArrow(@NotNull final String data) {
    return ItemBuilder.from(
            SkullCreator.itemFromBase64(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWNkYjhmNDM2NTZjMDZjNGU4NjgzZTJlNjM0MWI0NDc5ZjE1N2Y0ODA4MmZlYTRhZmYwOWIzN2NhM2M2OTk1YiJ9fX0="))
        .name(text(String.format("Increase %s by One", data), GREEN))
        .build();
  }

  private ItemStack getDecreaseArrow(@NotNull final String data) {
    return ItemBuilder.from(
            SkullCreator.itemFromBase64(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFlMWU3MzBjNzcyNzljOGUyZTE1ZDhiMjcxYTExN2U1ZTJjYTkzZDI1YzhiZTNhMDBjYzkyYTAwY2MwYmI4NSJ9fX0="))
        .name(text(String.format("Decrease %s by One", data), RED))
        .build();
  }
}
