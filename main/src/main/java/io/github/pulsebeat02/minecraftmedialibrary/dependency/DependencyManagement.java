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

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.utility.DependencyUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import io.github.slimjar.relocation.RelocationRule;
import io.github.slimjar.relocation.facade.JarRelocatorFacadeFactory;
import io.github.slimjar.relocation.facade.ReflectiveJarRelocatorFacadeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the full dependency management class, which handles the dependencies of the project by
 * managing them. It first creates a folder if it cannot be found. Then, it creates a relocation
 * folder for the relocated jars. After that, the methods are up to the user to decide when to
 * install, load, or relocate the binaries.
 */
public class DependencyManagement {

  private static final ExecutorService EXECUTOR_SERVICE;

  static {
    EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  }

  private final Set<Path> files;
  private final Path dir;
  private final Path relocatedDir;

  /**
   * Instantiates a new DependencyManagement.
   *
   * @param library instance
   */
  public DependencyManagement(@NotNull final MediaLibrary library) {
    this(library.getDependenciesFolder());
  }

  /**
   * Instantiates a new DependencyManagement.
   *
   * @param dirPath directory
   */
  public DependencyManagement(@NotNull final Path dirPath) {
    files = new HashSet<>();
    dir = dirPath;
    if (Files.notExists(dir)) {
      try {
        Files.createDirectory(dir);
        Logger.info(
            String.format(
                "Dependency Directory (%s) does not exist... Creating a folder",
                dir.toAbsolutePath()));
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
    relocatedDir = dir.resolve("relocated");
    if (Files.notExists(relocatedDir)) {
      try {
        Files.createDirectory(relocatedDir);
        Logger.info(
            String.format(
                "Relocated Directory (%s) does not exist... Creating a folder",
                relocatedDir.toAbsolutePath()));
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /** Installs all libraries from links. */
  public void install() {
    final List<Callable<Object>> tasks = new ArrayList<>();
    for (final RepositoryDependency dependency : RepositoryDependency.values()) {
      if (!checkExists(relocatedDir, dependency)) {
        tasks.add(Executors.callable(() -> installDependency(dependency)));
      }
    }
    try {
      EXECUTOR_SERVICE.invokeAll(tasks);
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Installs a specific dependency.
   *
   * @param dependency the repository dependency
   */
  private void installDependency(@NotNull final RepositoryDependency dependency) {
    final String artifact = dependency.getArtifact();
    final DependencyResolution resolution = dependency.getResolution();
    Path file = null;
    if (resolution == DependencyResolution.MAVEN_DEPENDENCY) {
      Logger.info(String.format("Checking Maven Central Repository for %s", artifact));
      try {
        file =
            DependencyUtilities.downloadMavenDependency(
                dependency, dir.toAbsolutePath().toString());
      } catch (final IOException e) {
        Logger.info(String.format("Could NOT find %s in Maven Central Repository!", artifact));
        e.printStackTrace();
      }

    } else if (resolution == DependencyResolution.JITPACK_DEPENDENCY) {
      Logger.info(String.format("Checking Jitpack Central Repository for %s", artifact));
      try {
        file =
            DependencyUtilities.downloadJitpackDependency(
                dependency, dir.toAbsolutePath().toString());
      } catch (final IOException e) {
        Logger.info(String.format("Could NOT find %s in Jitpack Central Repository!", artifact));
        e.printStackTrace();
      }
    }
    if (file != null) {
      files.add(file);
    }
  }

  /** Relocates Dependencies. */
  public void relocate() {
    try {
      final JarRelocatorFacadeFactory factory = ReflectiveJarRelocatorFacadeFactory.create();
      final List<RelocationRule> relocations =
          Arrays.stream(JarRelocationConvention.values())
              .map(JarRelocationConvention::getRelocation)
              .collect(Collectors.toList());
      final List<Callable<Object>> tasks = new ArrayList<>();
      for (final Path f : files) {
        tasks.add(
            Executors.callable(
                () -> {
                  try {
                    factory
                        .createFacade(
                            f.toFile(), relocatedDir.resolve(f.getFileName()).toFile(), relocations)
                        .run();
                  } catch (final IOException
                      | IllegalAccessException
                      | InstantiationException
                      | InvocationTargetException e) {
                    e.printStackTrace();
                  }
                }));
      }
      EXECUTOR_SERVICE.invokeAll(tasks);
    } catch (final URISyntaxException
        | ReflectiveOperationException
        | NoSuchAlgorithmException
        | IOException
        | InterruptedException e) {
      e.printStackTrace();
    }
  }

  /** Install and load. */
  public void load() {
    try (final Stream<Path> paths = Files.walk(relocatedDir)) {
      paths
          .filter(Files::isRegularFile)
          .forEach(
              x -> {
                try {
                  DependencyUtilities.loadDependency(x);
                } catch (final IOException e) {
                  e.printStackTrace();
                }
              });
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /** Deletes all the stale dependency files. */
  public void delete() {
    for (final Path file : files) {
      try {
        Files.delete(file);
        Logger.info(String.format("Finished Initializing Dependency (%s)", file.toAbsolutePath()));
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
    files.clear();
  }

  /**
   * Check if dependency exists in the directory beforehand.
   *
   * @param dir the directory
   * @param dependency the dependency
   * @return the boolean
   */
  private boolean checkExists(
      @NotNull final Path dir, @NotNull final RepositoryDependency dependency) {
    if (Files.notExists(dir)) {
      return false;
    }
    try (final Stream<Path> paths = Files.walk(dir)) {
      return paths.anyMatch(
          x ->
              Files.isRegularFile(x)
                  && PathUtilities.getName(x).contains(dependency.getArtifact()));
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Get the files that were downloaded.
   *
   * @return the set of files downloaded
   */
  public Set<Path> getFiles() {
    return files;
  }
}
