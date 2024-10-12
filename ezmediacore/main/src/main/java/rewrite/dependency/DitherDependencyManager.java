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
package rewrite.dependency;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import rewrite.json.GsonProvider;
import io.github.pulsebeat02.ezmediacore.utility.io.ResourceUtils;
import rewrite.natives.NativeLibraryLoader;
import rewrite.util.os.Platform;
import rewrite.natives.strategy.implementation.NativeResourceLocator;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Map;

import rewrite.natives.strategy.implementation.UrlResourceNativeLibrary;


public final class DitherDependencyManager extends LibraryDependency {

  private static final String DITHER_RESOURCE_PATH = "/emc-json/dither/binaries.json";

  public static final Map<Platform, NativeResourceLocator> NATIVE_LIBRARY_MAP;

  static {
    final Gson gson = GsonProvider.getSimple();
    try (final Reader reader = ResourceUtils.getResourceAsInputStream(DITHER_RESOURCE_PATH)) {
      final TypeToken<Map<Platform, UrlResourceNativeLibrary>> token = new TypeToken<>() {};
      final Type type = token.getType();
      NATIVE_LIBRARY_MAP = gson.fromJson(reader, type);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public DitherDependencyManager(final EzMediaCore core) {
    super(core);
  }

  @Override
  public void start() {
    final NativeLibraryLoader.Builder builder = NativeLibraryLoader.builder();
    NATIVE_LIBRARY_MAP.forEach(builder::addNativeLibrary);
    try {
      final NativeLibraryLoader loader = builder.build();
      loader.load(true);
    } catch (
        final UnsatisfiedLinkError ignored) { // suppress because native libraries aren't supported
    }
  }

  @Override
  public void onInstallation( final Path path) {}
}
