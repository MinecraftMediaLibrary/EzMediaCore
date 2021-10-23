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
package io.github.pulsebeat02.ezmediacore.dependency;

import io.github.slimjar.injector.DependencyInjector;
import io.github.slimjar.injector.loader.Injectable;
import io.github.slimjar.resolver.ResolutionResult;
import io.github.slimjar.resolver.data.DependencyData;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public record JarInjector(Collection<URL> dependencies) implements
    DependencyInjector {

  public JarInjector(@NotNull final Collection<URL> dependencies) {
    this.dependencies = dependencies;
  }

  @Override
  public void inject(
      @NotNull final Injectable injectable,
      @NotNull final DependencyData data,
      @NotNull final Map<String, ResolutionResult> var3) {
    this.dependencies.forEach(
        x -> {
          try {
            injectable.inject(x);
          } catch (final InvocationTargetException
              | IllegalAccessException
              | IOException
              | URISyntaxException e) {
            e.printStackTrace();
          }
        });
  }
}
