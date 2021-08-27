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

import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.resolver.data.DependencyData;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;

public final class JarLoader {

  private final Collection<URL> urls;

  public JarLoader(@NotNull final Collection<Path> paths) {
    this.urls = new ArrayList<>();
    paths.forEach(
        url -> {
          try {
            this.urls.add(new URL(url.toUri().toString().replace("\\", "/").replace("%20", " ")));
          } catch (final MalformedURLException e) {
            e.printStackTrace();
          }
        });
  }

  public void inject() {
    try {
      ApplicationBuilder.appending("EzMediaCore")
          .injectorFactory(helper -> new JarInjector(this.urls))
          .dataProviderFactory(
              (url) ->
                  () ->
                      new DependencyData(
                          Collections.emptyList(),
                          Collections.emptyList(),
                          Collections.emptyList(),
                          Collections.emptyList()))
          .build();
    } catch (final URISyntaxException
        | ReflectiveOperationException
        | NoSuchAlgorithmException
        | IOException e) {
      e.printStackTrace();
    }
  }
}
