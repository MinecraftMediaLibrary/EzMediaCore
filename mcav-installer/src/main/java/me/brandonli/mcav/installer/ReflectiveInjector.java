/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.installer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;
import org.checkerframework.checker.nullness.qual.Nullable;

final class ReflectiveInjector extends UrlClassLoaderInjector {

  private static final String ADD_URL_METHOD = "addURL";

  static final @Nullable MethodHandle ADD_URL = findAddUrl();

  private final MethodHandle addUrlHandle;

  ReflectiveInjector(final URLClassLoader classLoader, final MethodHandle addUrlHandle) {
    super(classLoader);
    this.addUrlHandle = addUrlHandle;
  }

  private static @Nullable MethodHandle findAddUrl() {
    final MethodHandles.Lookup lookup = MethodHandles.lookup();
    return findAddUrl(lookup);
  }

  static @Nullable MethodHandle findAddUrl(final MethodHandles.Lookup lookup) {
    try {
      final MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(URLClassLoader.class, lookup);
      final MethodType type = MethodType.methodType(void.class, URL.class);
      return privateLookup.findVirtual(URLClassLoader.class, ADD_URL_METHOD, type);
    } catch (final IllegalAccessException | NoSuchMethodException | RuntimeException exception) {
      return null;
    }
  }

  @Override
  void addUrl(final URL url) {
    final URLClassLoader loader = this.getClassLoader();
    try {
      this.addUrlHandle.invoke(loader, url);
    } catch (final RuntimeException | Error exception) {
      throw exception;
    } catch (final Throwable exception) {
      final String message = "Jar %s could not be added to the class loader!".formatted(url);
      throw new JarInjectorException(message, exception);
    }
  }
}
