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
package io.github.pulsebeat02.ezmediacore.resourcepack;

import java.util.Map;

import io.github.pulsebeat02.ezmediacore.reflect.versioning.ServerEnvironment;
import org.bukkit.Bukkit;


public enum PackFormat {

  VER_1_21_R1(34),
  VER_UNKNOWN(-1);

  private static final PackFormat CURRENT_FORMAT;
  private static final String VERSION;
  private static final Map<String, PackFormat> KEYS;

  static {
    VERSION = ServerEnvironment.getNMSRevision();
    KEYS = Map.of("1_21_R1", VER_1_21_R1);
    CURRENT_FORMAT = KEYS.get(VERSION);
  }

  private final int id;

  PackFormat(final int id) {
    this.id = id;
  }

  public static PackFormat getCurrentFormat() {
    return CURRENT_FORMAT;
  }

  public int getId() {
    return this.id;
  }
}
