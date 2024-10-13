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
package rewrite.dither;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import rewrite.json.GsonProvider;
import io.github.pulsebeat02.ezmediacore.utility.io.ResourceUtils;

public final class MapPalette {

  private static final String PALETTE_PATH = "/emc-json/palette/colors.json";

  public static final Color[] NMS_PALETTE;

  static {
    final Gson gson = GsonProvider.getSimple();
    try (final Reader reader = ResourceUtils.getResourceAsInputStream(PALETTE_PATH)) {
      final TypeToken<int[][]> token = new TypeToken<>() {};
      final Type type = token.getType();
      final int[][] colors = gson.fromJson(reader, type);
      NMS_PALETTE =
          Stream.of(colors)
              .map(createColor())
              .toArray(Color[]::new);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Function<int[], Color> createColor() {
    return color -> new Color(color[0], color[1], color[2]);
  }

  public static Color getColor(final byte val) {
    return NMS_PALETTE[val];
  }
}
