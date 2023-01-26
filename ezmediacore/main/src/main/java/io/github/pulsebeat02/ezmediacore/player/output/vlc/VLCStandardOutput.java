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
package io.github.pulsebeat02.ezmediacore.player.output.vlc;

import io.github.pulsebeat02.ezmediacore.player.output.OutputConfiguration;
import java.util.Map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class VLCStandardOutput extends OutputConfiguration {

  public static final String ACCESS;
  public static final String MUX;
  public static final String DST;
  public static final String BIND;
  public static final String PATH;
  public static final String SAP;
  public static final String NAME;
  public static final String DESCRIPTION;
  public static final String URL;
  public static final String EMAIL;

  static {
    ACCESS = "access";
    MUX = "mux";
    DST = "dst";
    BIND = "bind";
    PATH = "path";
    SAP = "sap";
    NAME = "name";
    DESCRIPTION = "description";
    URL = "url";
    EMAIL = "email";
  }

  private final String section;

  protected VLCStandardOutput(@NotNull final String section) {
    this.section = section;
  }

  @Contract("_ -> new")
  public static @NotNull VLCStandardOutput ofSection(@NotNull final String section) {
    return new VLCStandardOutput(section);
  }

  @Override
  public @NotNull String toString() {
    final Map<String, String> configuration = this.getConfiguration();
    final StringBuilder builder = new StringBuilder("%s{".formatted(this.section));
    for (final Map.Entry<String, String> entry : configuration.entrySet()) {
      builder.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
    }
    builder.deleteCharAt(builder.length() - 1);
    builder.append("}");
    return builder.toString();
  }
}
