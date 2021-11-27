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

package io.github.pulsebeat02.deluxemediaplugin.command.gui;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand;
import static org.bukkit.ChatColor.translateAlternateColorCodes;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Easily create itemstacks, without messing your hands. <i>Note that if you do use this in one of
 * your projects, leave this notice.</i> <i>Please do credit me if you do use this in one of your
 * projects.</i>
 *
 * @author NonameSL
 */
@SuppressWarnings("deprecation")
public class ItemBuilder {

  private final ItemStack is;
  private Consumer<InventoryClickEvent> action;

  /**
   * Create a new ItemBuilder over an existing itemstack.
   *
   * @param is The itemstack to create the ItemBuilder over.
   */
  ItemBuilder(@NotNull final ItemStack is) {
    this.is = is;
  }

  @Contract("_ -> new")
  public static @NotNull ItemBuilder from(@NotNull final Material material) {
    return from(material, 1);
  }

  @Contract("_, _ -> new")
  public static @NotNull ItemBuilder from(@NotNull final Material material, final int count) {
    return from(new ItemStack(material, count));
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull ItemBuilder from(@NotNull final ItemStack stack) {
    return new ItemBuilder(stack);
  }

  /**
   * Clone the ItemBuilder into a new one.
   *
   * @return The cloned instance.
   */
  @Override
  public ItemBuilder clone() {
    return from(this.is);
  }

  /**
   * Change the durability of the item.
   *
   * @param dur The durability to set it to.
   */
  public @NotNull ItemBuilder durability(final short dur) {
    this.is.setDurability(dur);
    return this;
  }

  /**
   * Set the displayname of the item.
   *
   * @param name The name to change it to.
   */
  public @NotNull ItemBuilder name(@NotNull final Component name) {
    final ItemMeta im = this.is.getItemMeta();
    im.setDisplayName(translateAlternateColorCodes('&', legacyAmpersand().serialize(name)));
    this.is.setItemMeta(im);
    return this;
  }

  /**
   * Adds an inventory action.
   *
   * @param action the inventory action.
   */
  public @NotNull ItemBuilder action(@NotNull final Consumer<InventoryClickEvent> action) {
    this.action = action;
    return this;
  }

  /**
   * Creates a GuiItem using the itemstack and action from the builder.
   *
   * @return The GuiItem created using the itemstack and action.
   */
  public @NotNull GuiItem build() {
    return new GuiItem(this.is, this.action);
  }

  /**
   * Returns the itemstack from the builder.
   *
   * @return The itemstack created/modified in the builder.
   */
  public @NotNull ItemStack buildWithoutAction() {
    return this.is;
  }
}
