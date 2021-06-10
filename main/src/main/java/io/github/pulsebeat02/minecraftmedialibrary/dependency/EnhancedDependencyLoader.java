/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.dependency;

import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.resolver.data.DependencyData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Enhanced dependency loader which supports Java 16 and future Java versions. */
public class EnhancedDependencyLoader {

  private final List<URL> jars;
  private final boolean legacy;

  /** Instantiates a new EnhancedDependencyLoader */
  public EnhancedDependencyLoader() {
    this(new ArrayList<>());
  }

  /**
   * Instantiates a new EnhancedDependencyLoader
   *
   * @param paths the paths
   */
  public EnhancedDependencyLoader(@NotNull final List<URL> paths) {
    jars = paths;
    legacy = Integer.parseInt(System.getProperty("java.version").split("\\.")[1]) == 8;
  }

  /**
   * Adds a jar to the loading list.
   *
   * @param file the dependency
   */
  public void addJar(@NotNull final Path file) {
    try {
      if (legacy) {
        jars.add(file.toUri().toURL());
      } else {
        jars.add(new URL(String.format("jar:/%s", file)));
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /** Loads all the jars provided in an injection process. */
  public void loadJars() {
    try {
      ApplicationBuilder.appending("MinecraftMediaLibrary")
          .injectorFactory(helper -> new MMLDependencyInjector(jars))
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

  /**
   * Gets the list of jars.
   *
   * @return the jars to load
   */
  public List<URL> getJars() {
    return jars;
  }
}
