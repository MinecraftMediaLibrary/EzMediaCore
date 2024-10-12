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
package rewrite.locale;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import rewrite.json.GsonProvider;
import io.github.pulsebeat02.ezmediacore.utility.io.ResourceUtils;


import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class MessageLoader {

  private static final String LOCALE_PATH = "/emc-json/locale/english.json";

  private MessageLoader() {}

  private static final Map<String, String> INTERNAL_LOCALE;

  static {
    final Gson gson = GsonProvider.getSimple();
    try (final Reader reader = ResourceUtils.getResourceAsInputStream(LOCALE_PATH)) {
      final TypeToken<Map<String, String>> token = new TypeToken<>() {};
      final Type type = token.getType();
      INTERNAL_LOCALE = gson.fromJson(reader, type);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String key(final String key) {
    return requireNonNull(INTERNAL_LOCALE.get(key), "Missing translation key %s".formatted(key));
  }
}
