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
package rewrite.util.io;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import rewrite.EzMediaCore;
import rewrite.json.GsonProvider;
import rewrite.util.os.OS;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public final class ResourceUtils {

  private ResourceUtils() {
    throw new UnsupportedOperationException();
  }

  public static  InputStreamReader getResourceAsInputStream(
       final String resource) {
    return new InputStreamReader(
        requireNonNull(EzMediaCore.class.getResourceAsStream(resource)));
  }

  public static InputStreamReader getResourceAsStream(final String resource) {
    return new InputStreamReader(requireNonNull(ResourceUtils.class.getResourceAsStream(resource)));
  }

  public static Table<OS, Boolean, String> parseTable(final String resource) {
    final Gson gson = GsonProvider.getSimple();
    try (final Reader reader = getResourceAsStream(resource)) {
      final TypeToken<Map<OS, Map<Boolean, String>>> token = new TypeToken<>() {};
      final Type type = token.getType();
      final Map<OS, Map<Boolean, String>> map = gson.fromJson(reader, type);
      return getHashTable(map);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Table<OS, Boolean, String> getHashTable(final Map<OS, Map<Boolean, String>> map) {
    final Table<OS, Boolean, String> table = HashBasedTable.create();
    final Set<Map.Entry<OS, Map<Boolean, String>>> entries = map.entrySet();
    for (final Map.Entry<OS, Map<Boolean, String>> entry : entries) {
      final OS os = entry.getKey();
      final Map<Boolean, String> map1 = entry.getValue();
      map1.forEach((bits, path) -> table.put(os, bits, path));
    }
    return table;
  }
}
