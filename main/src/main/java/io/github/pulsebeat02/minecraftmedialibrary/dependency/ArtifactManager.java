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
import io.github.pulsebeat02.minecraftmedialibrary.utility.HashingUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import io.github.slimjar.relocation.RelocationRule;
import io.github.slimjar.relocation.facade.JarRelocatorFacadeFactory;
import io.github.slimjar.relocation.facade.ReflectiveJarRelocatorFacadeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the new improved dependency management class, which handles the dependencies of the
 * project by managing them. It first creates a folder if it cannot be found. Then, it creates a
 * relocation folder for the relocated jars. It checks for hashes of files and gives a warning if
 * one of the hashes for the dependencies are invalid.
 */
public class ArtifactManager {

  private static final ExecutorService EXECUTOR_SERVICE;
  private static JarRelocatorFacadeFactory RELOCATION_FACTORY;

  static {
    EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    try {
      RELOCATION_FACTORY = ReflectiveJarRelocatorFacadeFactory.create();
    } catch (final URISyntaxException
        | ReflectiveOperationException
        | NoSuchAlgorithmException
        | IOException e) {
      e.printStackTrace();
    }
  }

  private final Set<Path> jars;
  private final Set<String> hashes;
  private final Path dependencyPath;
  private final Path relocatedPath;
  private final Path hashCacheFile;

  /**
   * Instantiates an ArtifactManager.
   *
   * @param library the library
   */
  public ArtifactManager(@NotNull final MediaLibrary library) {
    this(library.getDependenciesFolder());
  }

  /**
   * Instantiates an ArtifactManager.
   *
   * @param dependency the dependency path
   */
  public ArtifactManager(@NotNull final Path dependency) {
    jars = new HashSet<>();
    hashes = new HashSet<>();
    dependencyPath = dependency.toAbsolutePath();
    relocatedPath = dependencyPath.resolve("relocated");
    hashCacheFile = dependencyPath.resolve(".relocated-dependency-hash");
  }

  /** Starts the dependency loading process. */
  public void start() {
    setup();
    install();
    relocate();
    writeHashes();
    load();
    delete();
  }

  /** Sets up all the necessary directories and files. */
  private void setup() {
    try {
      Files.createDirectories(dependencyPath);
      Files.createDirectories(relocatedPath);
      if (Files.notExists(hashCacheFile)) {
        Files.createFile(hashCacheFile);
      } else {
        try (final Stream<String> stream = Files.lines(hashCacheFile)) {
          stream.forEach(hashes::add);
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /** Installs all libraries from links. */
  public void install() {
    try {
      EXECUTOR_SERVICE.invokeAll(
          Arrays.stream(RepositoryDependency.values())
              .filter(x -> !checkDependencyExistance(x))
              .map(x -> Executors.callable(() -> installDependency(x)))
              .collect(Collectors.toList()));
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

  /** Loads the jars in the relocated directory. */
  private void load() {
    Logger.info("Loading All Dependencies into Runtime...");
    try {
      final Set<Path> invalid =
          Files.walk(relocatedPath)
              .filter(Files::isRegularFile)
              .filter(path -> !hashes.contains(HashingUtilities.getHash(path)))
              .collect(Collectors.toSet());
      if (!invalid.isEmpty()) {
        Logger.warn(
            String.format("Dependency %s has an invalid hash! Please note this issue.", invalid));
      }
      try (final Stream<Path> paths = Files.walk(relocatedPath)) {
        Logger.info(String.format("Loading Dependencies: %s", paths));
        DependencyUtilities.loadDependencies(
            paths
                .filter(Files::isRegularFile)
                .filter(x -> PathUtilities.getName(x).endsWith(".jar"))
                .collect(Collectors.toList()));
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Installs a specific dependency (continues to install until hash is correct).
   *
   * @param dependency the dependency
   */
  private void installDependency(@NotNull final RepositoryDependency dependency) {
    final DependencyResolution resolution = dependency.getResolution();
    final String artifact = dependency.getArtifact();
    final String path = dependencyPath.toString();
    Path file = null;
    try {
      switch (resolution) {
        case MAVEN_DEPENDENCY:
          Logger.info(String.format("Checking Maven Central Repository for %s", artifact));
          file = DependencyUtilities.downloadMavenDependency(dependency, path);
          break;
        case JITPACK_DEPENDENCY:
          Logger.info(String.format("Checking Jitpack Central Repository for %s", artifact));
          file = DependencyUtilities.downloadJitpackDependency(dependency, path);
          break;
        default:
          throw new IllegalStateException(
              "Specified Repository URL Doesn't Exist! (Not Maven/Jitpack)");
      }
    } catch (final IOException e) {
      Logger.info(
          String.format(
              "Cannot Resolve Dependency! (Artifact: %s | Repository URL: %s)",
              artifact, resolution.getBaseUrl()));
      e.printStackTrace();
    }
    if (file != null) {
      if (HashingUtilities.validateDependency(file, dependency)) {
        Logger.info(String.format("SHA1 Hash for File %s Succeeded!", file));
        jars.add(file);
      } else {
        try {
          Logger.info(
              String.format(
                  "SHA1 Hash for File %s Failed! Downloading the Dependency Again...", file));
          Files.delete(file);
          installDependency(dependency);
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Relocates a specific file.
   *
   * @param relocations the relocations
   * @param input the input jar
   */
  private void relocateFile(
      @NotNull final List<RelocationRule> relocations, @NotNull final Path input) {
    final File result = relocatedPath.resolve(input.getFileName()).toFile();
    try {
      RELOCATION_FACTORY.createFacade(input.toFile(), result, relocations).run();
      hashes.add(HashingUtilities.getHash(result.toPath()));
    } catch (final IOException
        | InvocationTargetException
        | IllegalAccessException
        | InstantiationException e) {
      e.printStackTrace();
    }
  }

  /** Relocates Dependencies. */
  private void relocate() {
    final List<RelocationRule> relocations =
        Arrays.stream(JarRelocationConvention.values())
            .map(JarRelocationConvention::getRelocation)
            .collect(Collectors.toList());
    Logger.info(String.format("Preparing to Relocate %d Dependencies (%s)", jars.size(), jars));
    try {
      EXECUTOR_SERVICE.invokeAll(
          jars.stream()
              .map(x -> Executors.callable(() -> relocateFile(relocations, x)))
              .collect(Collectors.toList()));
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

  /** Writes the hashes down onto a file. */
  private void writeHashes() {
    Logger.info("Writing Down Hashes...");
    try (final PrintWriter writer = new PrintWriter(Files.newBufferedWriter(hashCacheFile))) {
      hashes.forEach(writer::println);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /** Deletes all stale dependencies. */
  private void delete() {
    Logger.info("Deleting Stale Dependencies...");
    jars.forEach(
        file -> {
          try {
            Files.delete(file);
            Logger.info(String.format("Deleting Stale Dependency (%s)", file.toAbsolutePath()));
          } catch (final IOException e) {
            e.printStackTrace();
          }
        });
    jars.clear();
  }

  /**
   * Checks if the dependency exists in the relocated path.
   *
   * @param dependency the dependency
   * @return whether the dependency exists in the relocated path
   */
  private boolean checkDependencyExistance(@NotNull final RepositoryDependency dependency) {
    final String artifact = dependency.getArtifact();
    try (final Stream<Path> paths = Files.walk(relocatedPath)) {
      return paths
          .filter(Files::isRegularFile)
          .anyMatch(
              x -> {
                final String name = PathUtilities.getName(x);
                return name.contains(dependency.getArtifact())
                    && name.contains(dependency.getVersion());
              });
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return false;
  }
}
