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
import java.net.URL;
import java.net.URLClassLoader;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class UrlClassLoaderInjector {

  private final URLClassLoader classLoader;

  UrlClassLoaderInjector(final URLClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  static UrlClassLoaderInjector create(final URLClassLoader classLoader) {
    return create(classLoader, ReflectiveInjector.ADD_URL, UnsafeInjector.UNSAFE);
  }

  static UrlClassLoaderInjector create(
    final URLClassLoader classLoader,
    final @Nullable MethodHandle addUrlHandle,
    final UnsafeInjector.@Nullable UnsafeAccess unsafe
  ) {
    if (addUrlHandle != null) {
      return new ReflectiveInjector(classLoader, addUrlHandle);
    }

    if (unsafe != null) {
      return new UnsafeInjector(classLoader, unsafe);
    }

    final String message =
      "Jars cannot be added to a URLClassLoader on this runtime; add --add-opens java.base/java.net=ALL-UNNAMED to allow it!";
    throw new JarInjectorException(message);
  }

  abstract void addUrl(final URL url);

  URLClassLoader getClassLoader() {
    return this.classLoader;
  }
}
