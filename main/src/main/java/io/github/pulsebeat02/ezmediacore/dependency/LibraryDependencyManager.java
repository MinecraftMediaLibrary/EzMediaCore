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

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.resolver.data.Repository;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class LibraryDependencyManager {

  private final MediaLibraryCore core;

  public LibraryDependencyManager(@NotNull final MediaLibraryCore core)
      throws ReflectiveOperationException, URISyntaxException, NoSuchAlgorithmException, IOException {
    this.core = core;
    this.start();
  }

  private void start()
      throws ReflectiveOperationException, URISyntaxException, NoSuchAlgorithmException, IOException {
    ApplicationBuilder.appending("EzMediaCore - %s".formatted(this.core.getPlugin().getName()))
        .mirrorSelector((collection, collection1) -> collection)
        .downloadDirectoryPath(this.core.getDependencyPath())
        .internalRepositories(this.getRepositories())
        .logger((s, objects) -> this.core.getLogger().info(s.formatted(objects)))
        .build();
  }

  private @NotNull Set<Repository> getRepositories() {
    final Set<String> repos = Set.of(
        "https://repo1.maven.org/maven2",
        "https://repo.maven.apache.org/maven2/",
        "https://jitpack.io",
        "https://m2.dv8tion.net/releases");
    return repos.stream().map(this::getRepository).collect(Collectors.toUnmodifiableSet());
  }

  private @NotNull Repository getRepository(final String str) {
    try {
      return new Repository(new URL(str));
    } catch (final MalformedURLException e) {
      e.printStackTrace();
    }
    throw new AssertionError("Repository is down! Check connection?");
  }
}
