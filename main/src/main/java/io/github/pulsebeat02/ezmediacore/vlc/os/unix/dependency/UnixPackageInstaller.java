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
