/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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

import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.ezmediacore.utility.MapUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ScreenBuilderGui {

  private final ChestGui gui;
  private final StaticPane pane;
  private final Player viewer;
  private final DeluxeMediaPlugin plugin;
  private Material material;
  private int width;
  private int height;
  private int id;

  public ScreenBuilderGui(@NotNull final DeluxeMediaPlugin plugin, @NotNull final Player player) {
    this.gui = new ChestGui(5, ComponentHolder.of(text("Choose Screen Size", GOLD)));
    this.pane = new StaticPane(9, 5);
    this.plugin = plugin;
    this.material = Material.OAK_PLANKS;
    this.viewer = player;
    this.width = 5;
    this.height = 5;
    this.id = 0;
    this.initialize();
    this.gui.show(player);
  }

  private void initialize() {
    this.gui.setOnGlobalClick(x -> x.setCancelled(true));
    final GuiItem increaseWidth =
        new GuiItem(
            this.getIncreaseArrow("Width"),
            x -> {
              this.width++;
              this.update();
            });
    final GuiItem decreaseWidth =
        new GuiItem(
            this.getDecreaseArrow("Width"),
            x -> {
              this.width--;
              this.update();
            });
    final GuiItem increaseHeight =
        new GuiItem(
            this.getIncreaseArrow("Height"),
            x -> {
              this.height++;
              this.update();
            });
    final GuiItem decreaseHeight =
        new GuiItem(
            this.getDecreaseArrow("Height"),
            x -> {
              this.height--;
              this.update();
            });
    final GuiItem increaseID =
        new GuiItem(
            this.getIncreaseArrow("Map ID"),
            x -> {
              this.id++;
              this.update();
            });
    final GuiItem decreaseID =
        new GuiItem(
            this.getDecreaseArrow("Map ID"),
            x -> {
              this.id--;
              this.update();
            });
    final GuiItem buildScreen =
        ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE)
            .name(text("Build Screen", GREEN))
            .action(
                x -> {
                  this.viewer.closeInventory();
                  MapUtils.buildMapScreen(
                      this.viewer, this.material, this.width, this.height, this.id);
                  this.plugin
                      .audience()
                      .sender(this.viewer)
                      .sendMessage(format(text("Successfully built your new screen!", GREEN)));
                  this.viewer.playSound(
                      this.viewer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
                })
            .build();
    this.pane.addItem(buildScreen, 2, 8);
    this.pane.addItem(increaseWidth, 1, 1);
    this.pane.addItem(decreaseWidth, 3, 1);
    this.pane.addItem(increaseHeight, 1, 4);
    this.pane.addItem(decreaseHeight, 3, 3);
    this.pane.addItem(increaseID, 1, 5);
    this.pane.addItem(decreaseID, 3, 5);
    this.gui.addPane(this.pane);
    this.update();
  }

  private void update() {

    final GuiItem materialItem =
        ItemBuilder.from(this.material)
            .name(
                join(
                    noSeparators(),
                    text("Material - ", GOLD),
                    text(this.material.toString(), AQUA)))
            .build();
    materialItem.setAction(
        x -> {
          final ItemStack stack = x.getCursor();
          if (stack == null) {
            return;
          }
          this.material = stack.getType();
          this.pane.addItem(
              ItemBuilder.from(this.material)
                  .name(
                      join(
                          noSeparators(),
                          text("Material - ", GOLD),
                          text(this.material.toString(), AQUA)))
                  .build(),
              2,
              1);
        });
    this.pane.addItem(materialItem, 2, 1);

    final GuiItem widthItem =
        ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(text("Screen Width (%d Blocks)".formatted(this.width), GOLD))
            .build();
    this.pane.addItem(widthItem, 2, 4);

    final GuiItem heightItem =
        ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(text("Screen Height (%d Blocks)".formatted(this.height), GOLD))
            .build();
    this.pane.addItem(heightItem, 0, 6);

    final GuiItem idItem =
        ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(text("Starting Map ID (%d)".formatted(this.id), GOLD))
            .build();
    this.pane.addItem(idItem, 2, 8);
  }

  private @NotNull ItemStack getIncreaseArrow(@NotNull final String data) {
    return ItemBuilder.from(
            SkullCreator.itemFromBase64(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWNkYjhmNDM2NTZjMDZjNGU4NjgzZTJlNjM0MWI0NDc5ZjE1N2Y0ODA4MmZlYTRhZmYwOWIzN2NhM2M2OTk1YiJ9fX0="))
        .name(text("Increase %s by One".formatted(data), GREEN))
        .buildWithoutAction();
  }

  private @NotNull ItemStack getDecreaseArrow(@NotNull final String data) {
    return ItemBuilder.from(
            SkullCreator.itemFromBase64(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFlMWU3MzBjNzcyNzljOGUyZTE1ZDhiMjcxYTExN2U1ZTJjYTkzZDI1YzhiZTNhMDBjYzkyYTAwY2MwYmI4NSJ9fX0="))
        .name(text("Decrease %s by One".formatted(data), RED))
        .buildWithoutAction();
  }
}
