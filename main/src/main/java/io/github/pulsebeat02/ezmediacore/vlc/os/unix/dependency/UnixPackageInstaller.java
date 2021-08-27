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
package io.github.pulsebeat02.ezmediacore.vlc.os.unix.dependency;

import io.github.pulsebeat02.ezmediacore.utility.ArchiveUtils;
import io.github.pulsebeat02.ezmediacore.utility.DependencyUtils;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

public class UnixPackageInstaller {

  private final ExecutorService service;
  private final Path directory;
  private final Set<Path> archives;

  public UnixPackageInstaller(@NotNull final Path directory) {
    this.service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    this.directory = directory;
    this.archives = new HashSet<>();
  }

  public void start() {
    try {
      this.download();
      this.extract();
      this.shutdown();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void download() throws InterruptedException {
    this.service.invokeAll(
        Arrays.stream(VLCDependency.values())
            .map(dependency -> Executors.callable(() -> {
                  final String url = dependency.getDependency().getUrl();
                  try {
                    this.archives.add(DependencyUtils.downloadFile(
                        this.directory.resolve(
                            dependency.getDependency().getName() + "." + FilenameUtils.getExtension(
                                new URL(url).getPath())), url));
                  } catch (final IOException e) {
                    e.printStackTrace();
                  }
                }, null)
            ).collect(Collectors.toSet()));
  }

  private void extract() throws InterruptedException {
    this.service.invokeAll(this.archives.stream().map(
        path -> Executors.callable(() -> ArchiveUtils.decompressArchive(path,
                path.getParent().resolve(FilenameUtils.removeExtension(
                    PathUtils.getName(path)))),
            null)).collect(
        Collectors.toSet()));
  }

  private void shutdown() {
    this.service.shutdownNow();
  }

}
