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
package io.github.pulsebeat02.ezmediacore.utility.io;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.resourcepack.PackFormat;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.entity.Player;


public final class ResourcepackUtils {

  private ResourcepackUtils() {}

  public static boolean validatePackFormat(final int format) {
    return Stream.of(PackFormat.values()).anyMatch(value -> value.getId() == format);
  }

  public static boolean validateResourcepackIcon( final Path icon) {
    return PathUtils.getName(icon).endsWith(".png");
  }

  public static void forceResourcepackLoad(
       final EzMediaCore core,
       final Collection<? extends Player> players,
       final String url,
      final byte  [] hash) {
    checkArguments(core, url, hash);
    checkNotNull(players, "Specified players cannot be null!");
    new ForcefulResourcepackListener(
        core, players.stream().map(Player::getUniqueId).collect(Collectors.toSet()), url, hash);
  }

  private static void checkArguments(
       final EzMediaCore core,
       final String url,
      final byte  [] hash) {
    checkNotNull(core, "EzMediaCore cannot be null!");
    checkNotNull(url, "Resourcepack URL cannot be null!");
    checkNotNull(hash, "Resourcepack Hash cannot be null!");
  }
}
