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

import static java.util.Objects.requireNonNull;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

/**
 * A library for the Bukkit API to create player skulls from names, base64 strings, and texture
 * URLs.
 *
 * <p>Does not use any NMS code, and should work across all versions.
 *
 * @author Dean B on 12/28/2016.
 */
public final class SkullCreator {

  private static final boolean warningPosted = false;
  private static Field blockProfileField;
  private static MethodHandle metaSetProfileMethod;
  private static Field metaProfileField;

  private SkullCreator() {
  }

  /**
   * Creates a player skull, should work in both legacy and new Bukkit APIs.
   */
  public static @NotNull ItemStack createSkull() {
    return new ItemStack(Material.valueOf("PLAYER_HEAD"));
  }

  /**
   * Creates a player skull item with the skin based on a player's UUID.
   *
   * @param id The Player's UUID.
   * @return The head of the Player.
   */
  public static @NotNull ItemStack itemFromUuid(final UUID id) {
    return itemWithUuid(createSkull(), id);
  }

  /**
   * Creates a player skull item with the skin at a Mojang URL.
   *
   * @param url The Mojang URL.
   * @return The head of the Player.
   */
  public static @NotNull ItemStack itemFromUrl(final String url) {
    return itemWithUrl(createSkull(), url);
  }

  /**
   * Creates a player skull item with the skin based on a base64 string.
   *
   * @param base64 The Mojang URL.
   * @return The head of the Player.
   */
  public static @NotNull ItemStack itemFromBase64(final String base64) {
    return itemWithBase64(createSkull(), base64).orElseThrow(() -> new SkullException(base64));
  }

  /**
   * Modifies a skull to use the skin of the player with a given UUID.
   *
   * @param item The item to apply the name to. Must be a player skull.
   * @param id   The Player's UUID.
   * @return The head of the Player.
   */
  public static @NotNull ItemStack itemWithUuid(@NotNull final ItemStack item,
      @NotNull final UUID id) {
    final SkullMeta meta = requireNonNull((SkullMeta) item.getItemMeta());
    meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
    item.setItemMeta(meta);
    return item;
  }

  /**
   * Modifies a skull to use the skin at the given Mojang URL.
   *
   * @param item The item to apply the skin to. Must be a player skull.
   * @param url  The URL of the Mojang skin.
   * @return The head associated with the URL.
   */
  public static @NotNull ItemStack itemWithUrl(@NotNull final ItemStack item,
      @NotNull final String url) {
    final String base64 = urlToBase64(url);
    return itemWithBase64(item, base64).orElseThrow(() -> new SkullException(base64));
  }

  /**
   * Modifies a skull to use the skin based on the given base64 string.
   *
   * @param item   The ItemStack to put the base64 onto. Must be a player skull.
   * @param base64 The base64 string containing the texture.
   * @return The head with a custom texture.
   */
  public static Optional<ItemStack> itemWithBase64(
      @NotNull final ItemStack item, @NotNull final String base64) {
    if (!(item.getItemMeta() instanceof final SkullMeta meta)) {
      return Optional.empty();
    }
    mutateItemMeta(meta, base64);
    item.setItemMeta(meta);
    return Optional.of(item);
  }

  /**
   * Sets the block to a skull with the given UUID.
   *
   * @param block The block to set.
   * @param id    The player to set it to.
   */
  public static void blockWithUuid(@NotNull final Block block, @NotNull final UUID id) {
    setToSkull(block);
    final Skull state = (Skull) block.getState();
    state.setOwningPlayer(Bukkit.getOfflinePlayer(id));
    state.update(false, false);
  }

  /**
   * Sets the block to a skull with the skin found at the provided mojang URL.
   *
   * @param block The block to set.
   * @param url   The mojang URL to set it to use.
   */
  public static void blockWithUrl(@NotNull final Block block, @NotNull final String url) {
    blockWithBase64(block, urlToBase64(url));
  }

  /**
   * Sets the block to a skull with the skin for the base64 string.
   *
   * @param block  The block to set.
   * @param base64 The base64 to set it to use.
   */
  public static void blockWithBase64(@NotNull final Block block, @NotNull final String base64) {
    setToSkull(block);
    final Skull state = (Skull) block.getState();
    mutateBlockState(state, base64);
    state.update(false, false);
  }

  private static void setToSkull(final @NotNull Block block) {
    block.setType(Material.valueOf("PLAYER_HEAD"), false);
  }

  private static @NotNull String urlToBase64(final String url) {
    try {
      final String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}".formatted(new URI(url));
      return Base64.getEncoder().encodeToString(toEncode.getBytes());
    } catch (final URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private static @NotNull GameProfile makeProfile(final @NotNull String b64) {
    final UUID id =
        new UUID(
            b64.substring(b64.length() - 20).hashCode(),
            b64.substring(b64.length() - 10).hashCode());
    final GameProfile profile = new GameProfile(id, "Player");
    profile.getProperties().put("textures", new Property("textures", b64));
    return profile;
  }

  private static void mutateBlockState(final Skull block, final String b64) {
    try {
      Nill.ifSo(blockProfileField, () -> setBlockProfile(block));
      blockProfileField.set(block, makeProfile(b64));
    } catch (final IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private static void setBlockProfile(@NotNull final Skull block) {
    try {
      blockProfileField = block.getClass().getDeclaredField("profile");
      blockProfileField.setAccessible(true);
    } catch (final NoSuchFieldException e) {
      e.printStackTrace();
    }
  }

  private static void mutateItemMeta(@NotNull final SkullMeta meta, @NotNull final String b64) {
    try {
      Nill.ifSo(metaSetProfileMethod, () -> registerSetProfileMethod(meta));
      metaSetProfileMethod.invoke(meta, makeProfile(b64));
    } catch (final Throwable e) {
      e.printStackTrace();
    }
  }

  private static void registerSetProfileMethod(@NotNull final SkullMeta meta) {
    try {
      metaSetProfileMethod = MethodHandles.publicLookup()
          .findVirtual(meta.getClass(), "setProfile",
              MethodType.methodType(void.class, GameProfile.class));
    } catch (final NoSuchMethodException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
