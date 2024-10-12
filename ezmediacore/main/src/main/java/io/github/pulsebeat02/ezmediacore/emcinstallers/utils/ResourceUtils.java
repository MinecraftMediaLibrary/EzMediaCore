/**
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
package io.github.pulsebeat02.ezmediacore.emcinstallers.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.pulsebeat02.ezmediacore.emcinstallers.OS;
import io.github.pulsebeat02.emcinstallers.json.GsonProvider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class ResourceUtils {

  private ResourceUtils() {}

  public static InputStreamReader getResourceAsStream(final String resource) {
    return new InputStreamReader(requireNonNull(ResourceUtils.class.getResourceAsStream(resource)));
  }

  public static Table<OS, Boolean, String> parseTable(final String resource) {
    final Gson gson = GsonProvider.getGson();
    try (final Reader reader = getResourceAsStream(resource)) {
      final TypeToken<Map<OS, Map<Boolean, String>>> token = new TypeToken<>() {};
      final Map<OS, Map<Boolean, String>> map = gson.fromJson(reader, token.getType());
      return getHashTable(map);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Table<OS, Boolean, String> getHashTable(final Map<OS, Map<Boolean, String>> map) {
    final Table<OS, Boolean, String> table = HashBasedTable.create();
    map.forEach((os, map1) -> map1.forEach((bits, path) -> table.put(os, bits, path)));
    return table;
  }
}
