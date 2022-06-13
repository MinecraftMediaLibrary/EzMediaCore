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
package io.github.pulsebeat02.ezmediacore.resourcepack;

import java.util.Map;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public enum PackFormat {
  VER_117(7),
  VER_1171(7),
  VER_118(8),
  VER_1181(8),
  VER_119(9),
  VER_UNKNOWN(-1);

  private static final String VERSION;
  private static final Map<String, PackFormat> KEYS;

  static {
    VERSION =
        Bukkit.getServer()
            .getClass()
            .getPackage()
            .getName()
            .split("\\.")[3]
            .replaceAll("_", "")
            .replaceAll("R", "")
            .replaceAll("v", "");
    KEYS =
        Map.of(
            "117", VER_117,
            "1171", VER_1171,
            "118", VER_118,
            "1181", VER_1181,
            "119", VER_119);
  }

  private final int id;

  PackFormat(final int id) {
    this.id = id;
  }

  @NotNull
  public static PackFormat getCurrentFormat() {
    return KEYS.get(VERSION);
  }

  public int getId() {
    return this.id;
  }
}
