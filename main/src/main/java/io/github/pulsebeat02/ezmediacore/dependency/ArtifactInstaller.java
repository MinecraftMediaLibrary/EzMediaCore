package io.github.pulsebeat02.ezmediacore.dependency;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.sneaky.ThrowingConsumer;
import io.github.pulsebeat02.ezmediacore.utility.DependencyUtils;
import io.github.pulsebeat02.ezmediacore.utility.FileUtils;
import io.github.pulsebeat02.ezmediacore.utility.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import io.github.slimjar.relocation.RelocationRule;
import io.github.slimjar.relocation.facade.JarRelocatorFacadeFactory;
import io.github.slimjar.relocation.facade.ReflectiveJarRelocatorFacadeFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class ArtifactInstaller {

  private static final ExecutorService EXECUTOR_SERVICE;
  private static final List<RelocationRule> RELOCATION_RULES;

  static {
    EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    RELOCATION_RULES =
        Arrays.stream(RelocationPaths.values())
            .map(RelocationPaths::getRelocation)
            .collect(Collectors.toList());
  }

  private final JarRelocatorFacadeFactory factory;
  private final Set<Path> jars;
  private final Set<String> hashes;
  private final Path dependencyFolder;
  private final Path relocatedFolder;
  private final Path hashFile;

  public ArtifactInstaller(@NotNull final MediaLibraryCore core)
      throws ReflectiveOperationException, URISyntaxException, NoSuchAlgorithmException,
          IOException {
    this.factory = ReflectiveJarRelocatorFacadeFactory.create();
    this.jars = new HashSet<>();
    this.hashes = new HashSet<>();
    this.dependencyFolder = core.getDependencyPath();
    this.relocatedFolder = this.dependencyFolder.resolve("relocated");
    this.hashFile = this.dependencyFolder.resolve(".relocated-cache");
  }

  public void start() {
    try {
      createFiles();
      download();
      relocate();
      writeHashes();
      load();
      delete();
    } catch (final IOException | InterruptedException e) {
      Logger.info("A serious exception occurred during dependency instantiation!");
      e.printStackTrace();
    }
  }

  public void createFiles() throws IOException {
    Files.createDirectories(this.dependencyFolder);
    Files.createDirectories(this.relocatedFolder);
    if (!FileUtils.createFileIfNotExists(this.hashFile)) {
      try (final Stream<String> stream = Files.lines(this.hashFile)) {
        stream.forEach(this.hashes::add);
      }
    }
  }

  public void download() throws InterruptedException {
    EXECUTOR_SERVICE.invokeAll(
        Arrays.stream(DependencyInfo.values())
            .filter(this::requiresDownload)
            .map(path -> Executors.callable((PrivilegedAction<?>) () -> downloadDependency(path)))
            .collect(Collectors.toSet()));
  }

  public void relocate() throws InterruptedException {

    Logger.info(
        String.format("Preparing to relocate %d dependencies (%s)", this.jars.size(), this.jars));

    EXECUTOR_SERVICE.invokeAll(
        this.jars.stream()
            .map(path -> Executors.callable(() -> relocateFile(path, this.factory)))
            .collect(Collectors.toList()));
  }

  public void writeHashes() throws IOException {

    Logger.info("Recording relocated JAR hashes");

    try (final PrintWriter writer = new PrintWriter(Files.newBufferedWriter(this.hashFile))) {
      this.hashes.forEach(writer::println);
    }
  }

  public void load() throws IOException {

    Logger.info(
        String.format("Preparing to load %d dependencies (%s)", this.jars.size(), this.jars));

    final Set<Path> invalid =
        Files.walk(this.relocatedFolder)
            .filter(Files::isRegularFile)
            .filter(path -> PathUtils.getName(path).endsWith(".jar"))
            .filter(path -> !this.hashes.contains(HashingUtils.getHash(path)))
            .collect(Collectors.toSet());

    if (!invalid.isEmpty()) {
      for (final Path p : invalid) {
        Logger.warn(
            String.format("Dependency %s has an invalid hash! Downloading dependency again...", p));
        redownload(p, getDependency(p).orElseThrow(AssertionError::new));
      }
    }

    new JarLoader(
            Files.walk(this.relocatedFolder)
                .filter(Files::isRegularFile)
                .filter(path -> PathUtils.getName(path).endsWith(".jar"))
                .collect(Collectors.toList()))
        .inject();
  }

  public void delete() {

    Logger.info(
        String.format(
            "Preparing to delete %d stale dependencies (%s)", this.jars.size(), this.jars));

    this.jars.forEach(ThrowingConsumer.unchecked(Files::delete, "Could not delete dependency!"));

    this.jars.clear();
  }

  private boolean requiresDownload(@NotNull final DependencyInfo dependency) {
    try (final Stream<Path> paths = Files.walk(this.relocatedFolder)) {
      return paths
          .filter(Files::isRegularFile)
          .anyMatch(
              path -> {
                final String name = PathUtils.getName(path);
                return name.contains(dependency.getArtifact())
                    && name.contains(dependency.getVersion());
              });
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  @NotNull
  private Path downloadDependency(@NotNull final DependencyInfo dependency) {

    final Repositories resolution = dependency.getResolution();
    final String artifact = dependency.getArtifact();
    final String path = this.dependencyFolder.toString();

    Optional<Path> file;
    try {
      switch (resolution) {
        case MAVEN:
          Logger.info(String.format("Checking Maven Central Repository for %s", artifact));
          file = Optional.of(DependencyUtils.downloadMavenDependency(dependency, path));
          break;
        case JITPACK:
          Logger.info(String.format("Checking Jitpack Central Repository for %s", artifact));
          file = Optional.of(DependencyUtils.downloadJitpackDependency(dependency, path));
          break;
        default:
          throw new IllegalStateException(
              "Specified Repository URL Doesn't Exist! (Not Maven/Jitpack)");
      }
    } catch (final IOException e) {
      file = Optional.empty();
      Logger.info(
          String.format(
              "Cannot Resolve Dependency! (Artifact: %s | Repository URL: %s)",
              artifact, resolution.getUrl()));
      e.printStackTrace();
    }

    if (file.isPresent()) {
      final Path p = file.get();
      if (DependencyUtils.validateDependency(p, dependency)) {
        Logger.info(String.format("SHA1 Hash for File %s Succeeded!", file));
        this.jars.add(p);
      } else {
        try {
          Logger.info(
              String.format(
                  "SHA1 Hash for File %s Failed! Downloading the Dependency Again...", file));
          Files.delete(p);
          return downloadDependency(dependency);
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    } else {
      /*
      This should never happen. But if it does, it just retries
      Yeah that is a mess, but its a placeholder for the warning.
       */
      return downloadDependency(dependency);
    }

    return file.get();
  }

  private void relocateFile(
      @NotNull final Path input, @NotNull final JarRelocatorFacadeFactory factory) {
    final File result = this.relocatedFolder.resolve(input.getFileName()).toFile();
    try {
      factory.createFacade(input.toFile(), result, RELOCATION_RULES).run();
      this.hashes.add(HashingUtils.getHash(result.toPath()));
    } catch (final IOException
        | InvocationTargetException
        | IllegalAccessException
        | InstantiationException e) {
      e.printStackTrace();
    }
  }

  private void redownload(@NotNull final Path invalid, @NotNull final DependencyInfo dependency)
      throws IOException {
    Files.delete(invalid);
    relocateFile(downloadDependency(dependency), this.factory);
  }

  @NotNull
  private Optional<DependencyInfo> getDependency(@NotNull final Path jar) {
    for (final DependencyInfo dependency : DependencyInfo.values()) {
      final String name = PathUtils.getName(jar);
      if (name.contains(dependency.getArtifact()) && name.contains(dependency.getVersion())) {
        return Optional.of(dependency);
      }
    }
    return Optional.empty();
  }
}
