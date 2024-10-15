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
package io.github.pulsebeat02.ezmediacore.natives.strategy;

import io.github.pulsebeat02.ezmediacore.natives.strategy.implementation.FileSystemResourceNativeLibrary;
import io.github.pulsebeat02.ezmediacore.natives.strategy.implementation.JarResourceNativeLibrary;
import io.github.pulsebeat02.ezmediacore.natives.strategy.implementation.ResourceLocator;
import io.github.pulsebeat02.ezmediacore.natives.strategy.implementation.UrlResourceNativeLibrary;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/** Native library discovery options (local file, jar resource, or url). */
public enum LibraryLocation {
  LOCAL_RESOURCE(FileSystemResourceNativeLibrary.class),
  JAR_RESOURCE(JarResourceNativeLibrary.class),
  URL_RESOURCE(UrlResourceNativeLibrary.class);

  private final Class<?> clazz;

  LibraryLocation(final Class<?> clazz) {
    this.clazz = clazz;
  }

  /**
   * Creates a ResourceLocator based on the resource and the resource configuration provided.
   *
   * @param resource the resource configuration
   * @return a new ResourceLocator
   */
  public ResourceLocator create(final String resource) {
    try {
      final MethodHandles.Lookup lookup = MethodHandles.lookup();
      final MethodType methodType = MethodType.methodType(void.class, String.class);
      final MethodHandle constructor = lookup.findConstructor(this.clazz, methodType);
      return (ResourceLocator) constructor.invoke(resource);
    } catch (final Throwable e) {
      throw new AssertionError(e);
    }
  }
}
