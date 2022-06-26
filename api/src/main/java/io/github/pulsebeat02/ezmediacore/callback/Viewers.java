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

package io.github.pulsebeat02.ezmediacore.callback;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class Viewers {

  private final UUID[] viewers;
  private final Set<Player> players;

  Viewers(final UUID @NotNull [] viewers) {
    checkNotNull(viewers, "Viewers cannot be null!");
    this.viewers = viewers;
    this.players = Collections.newSetFromMap(new WeakHashMap<>());
    this.players.addAll(Arrays.stream(viewers).map(Bukkit::getPlayer).collect(Collectors.toList()));
  }

  public static @NotNull Viewers ofPlayers(@NotNull final Collection<? extends Player> collection) {
    return new Viewers(collection.stream().map(Entity::getUniqueId).toArray(UUID[]::new));
  }

  public static @NotNull Viewers ofPlayers(@NotNull final Player... viewers) {
    return ofPlayers(Arrays.stream(viewers).collect(Collectors.toList()));
  }

  public static @NotNull Viewers ofUUIDs(@NotNull final Collection<? extends UUID> collection) {
    return ofUUIDs(collection.toArray(UUID[]::new));
  }

  public static @NotNull Viewers ofUUIDs(@NotNull final UUID... viewers) {
    return new Viewers(viewers);
  }

  public static @NotNull Viewers onlinePlayers() {
    return ofPlayers(Bukkit.getOnlinePlayers());
  }

  public UUID @NotNull [] getViewers() {
    return this.viewers;
  }

  public @NotNull Set<Player> getPlayers() {
    return this.players;
  }
}
