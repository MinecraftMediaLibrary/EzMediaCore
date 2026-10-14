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
package me.brandonli.mcav.capability.installer.debian;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import me.brandonli.mcav.capability.installer.Download;
import me.brandonli.mcav.capability.installer.FileMode;
import me.brandonli.mcav.capability.installer.InstallationException;
import me.brandonli.mcav.capability.installer.Installer;
import me.brandonli.mcav.utils.FileOperationException;
import me.brandonli.mcav.utils.IoUtils;
import me.brandonli.mcav.utils.http.DownloadException;
import me.brandonli.mcav.utils.http.HttpDownloader;
import me.brandonli.mcav.utils.os.Arch;
import me.brandonli.mcav.utils.os.Platform;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Installs the shared libraries of pinned Debian packages into the cache folder of MCAV and loads them, so a Linux
 * machine without a library such as VLC can use it without a package manager.
 *
 * <p>The packages come from the Debian bookworm archive on {@code snapshot.debian.org}, in the lists
 * {@code mcav/common/installers/debian-vlc.json} and {@code mcav/common/installers/debian-gtk2.json}, which list each
 * package after the packages it depends on, with its SHA-256. Every package is downloaded, checked against its hash,
 * and its {@code usr/lib} and {@code lib} trees are extracted by MCAV itself, without {@code dpkg} and without
 * administrator rights, into one folder of the cache. The libraries need the glibc 2.36 and GCC 12 runtime of Debian
 * bookworm or newer, which the lists leave out.
 *
 * <p>{@link #loadLibraries()} loads every library of the installation into the JVM, the libraries of each package
 * after those of the packages before it, so that a library that later loads one of them by name, such as a VLC
 * plugin, finds it already loaded. A library that does not load, for example because the machine has an older
 * glibc, is logged and skipped.
 *
 * <p>This class is thread-safe.
 *
 * @since 1.0.0
 */
public final class DebianLibraryInstaller implements Installer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DebianLibraryInstaller.class);

  private static final String VLC_RESOURCE = "mcav/common/installers/debian-vlc.json";
  private static final String VLC_NAME = "debian-vlc";
  private static final String GTK2_RESOURCE = "mcav/common/installers/debian-gtk2.json";
  private static final String GTK2_NAME = "debian-gtk2";

  private static final long MAXIMUM_PACKAGE_SIZE = 64L * 1024L * 1024L;

  private static final String LIBRARY_TREE = "usr/lib";
  private static final String LEGACY_LIBRARY_TREE = "lib";
  private static final String X86_64_TRIPLET = "x86_64-linux-gnu";
  private static final String AARCH64_TRIPLET = "aarch64-linux-gnu";
  private static final String LIBRARY_PREFIX = "lib";
  private static final String LIBRARY_SUFFIX = ".so";

  private static final String LOAD_ORDER_FILE = "load-order.txt";
  private static final String EXTRACTED_FOLDER = "root";
  private static final String PACKAGE_FILE = "package.deb";

  private final Path folder;
  private final String name;
  private final Download[] downloads;
  private final Platform platform;
  private final Consumer<String> libraryLoader;

  // loading a native library is what this installer is for; the JVM warns about it unless native access is enabled
  @SuppressWarnings("restricted")
  @VisibleForTesting
  DebianLibraryInstaller(final Path folder, final String name, final Download[] downloads, final Platform platform) {
    this(folder, name, downloads, platform, System::load);
  }

  @VisibleForTesting
  DebianLibraryInstaller(
    final Path folder,
    final String name,
    final Download[] downloads,
    final Platform platform,
    final Consumer<String> libraryLoader
  ) {
    this.folder = folder;
    this.name = name;
    this.downloads = downloads.clone();
    this.platform = platform;
    this.libraryLoader = libraryLoader;
  }

  /**
   * Creates an installer of {@code libvlc5}, {@code libvlccore9} and {@code vlc-plugin-base} with their dependencies,
   * which has packages for 64-bit ARM Linux.
   *
   * @return the installer
   * @throws FileOperationException if the package list or the cache folder cannot be read
   * @since 1.0.0
   */
  public static DebianLibraryInstaller createVlcInstaller() {
    return create(VLC_RESOURCE, VLC_NAME);
  }

  /**
   * Creates an installer of {@code libgtk2.0-0} with its dependencies, which has packages for 64-bit x86 and ARM
   * Linux. OpenCV needs GTK 2 to load its object detection module on Linux.
   *
   * @return the installer
   * @throws FileOperationException if the package list or the cache folder cannot be read
   * @since 1.0.0
   */
  public static DebianLibraryInstaller createGtk2Installer() {
    return create(GTK2_RESOURCE, GTK2_NAME);
  }

  private static DebianLibraryInstaller create(final String resource, final String name) {
    final Download[] downloads = IoUtils.readDownloadsFromJsonResource(resource);
    final Path cache = IoUtils.getCachedFolder();
    final Path folder = cache.resolve(name);
    final Platform current = Platform.getCurrentPlatform();
    return new DebianLibraryInstaller(folder, name, downloads, current);
  }

  /**
   * Downloads and extracts the packages, or returns the existing installation. The packages are extracted into a
   * temporary folder next to the installation, which is moved into place once every package is extracted, so a failed
   * installation leaves nothing behind.
   *
   * @param mode ignored, because libraries are loaded and not run
   * @return the library folder of the installation, such as {@code usr/lib/aarch64-linux-gnu}
   * @throws InstallationException  if this platform has no packages, a package has no pinned hash, or a package cannot
   *                                be extracted or moved
   * @throws DownloadException      if a download fails or does not match its hash
   * @throws FileOperationException if a broken earlier installation or the temporary folder cannot be deleted
   * @throws NullPointerException   if {@code mode} is null
   * @since 1.0.0
   */
  @Override
  public synchronized Path download(final FileMode mode) {
    Preconditions.checkNotNull(mode, "Mode must not be null!");

    final Optional<Path> existing = this.findInstallation();
    if (existing.isPresent()) {
      return this.getPath();
    }

    final List<Download> packages = this.findPackages();
    if (packages.isEmpty()) {
      final String message = "Platform %s has no %s packages!".formatted(this.platform, this.name);
      throw new InstallationException(message);
    }

    this.deleteBrokenInstallation();
    try {
      this.install(packages);
    } catch (final IOException exception) {
      final String message = "Debian packages of %s could not be installed to %s!".formatted(this.name, this.folder);
      throw new InstallationException(message, exception);
    }

    return this.getPath();
  }

  // an installation folder without a load order was left by an older or interrupted installation
  private void deleteBrokenInstallation() {
    final boolean exists = Files.exists(this.folder);
    if (exists) {
      IoUtils.deleteRecursively(this.folder);
    }
  }

  private List<Download> findPackages() {
    final List<Download> packages = new ArrayList<>();
    for (final Download download : this.downloads) {
      final Platform downloadPlatform = download.getPlatform();
      if (downloadPlatform.equals(this.platform)) {
        packages.add(download);
      }
    }

    return packages;
  }

  private void install(final List<Download> packages) throws IOException {
    final Path parent = this.getParent();
    Files.createDirectories(parent);
    final Path staging = Files.createTempDirectory(parent, this.name);
    try {
      final Path extracted = staging.resolve(EXTRACTED_FOLDER);
      final List<String> loadOrder = new ArrayList<>();
      for (final Download download : packages) {
        this.installPackage(download, staging, extracted, loadOrder);
      }

      Files.createDirectories(extracted);
      final Path loadOrderFile = extracted.resolve(LOAD_ORDER_FILE);
      Files.write(loadOrderFile, loadOrder, StandardCharsets.UTF_8);
      Files.move(extracted, this.folder, StandardCopyOption.ATOMIC_MOVE);
    } finally {
      IoUtils.deleteRecursively(staging);
    }
  }

  private void installPackage(final Download download, final Path staging, final Path extracted, final List<String> loadOrder)
    throws IOException {
    final String url = download.getUrl();
    final Optional<String> hash = download.getHash();
    final @Nullable String sha256 = hash.orElse(null);
    if (sha256 == null) {
      final String message = "Debian package %s has no pinned hash!".formatted(url);
      throw new InstallationException(message);
    }

    final URI uri = URI.create(url);
    final Path archive = staging.resolve(PACKAGE_FILE);
    HttpDownloader.download(uri, archive, sha256, MAXIMUM_PACKAGE_SIZE);
    DebianPackageExtractor.extract(archive, extracted);

    final Set<String> libraries = this.listLibraries(extracted);
    libraries.removeAll(loadOrder);
    loadOrder.addAll(libraries);
  }

  // Lists the libraries directly in the library folders, sorted by name, as paths relative to the installation.
  private Set<String> listLibraries(final Path root) throws IOException {
    final Set<String> libraries = new LinkedHashSet<>();
    for (final Path directory : this.getLibraryDirectories(root)) {
      final List<Path> files = this.listDirectory(directory);
      final List<String> names = files.stream().filter(this::isLibrary).map(root::relativize).map(Path::toString).sorted().toList();
      libraries.addAll(names);
    }

    return libraries;
  }

  private List<Path> getLibraryDirectories(final Path root) {
    final String triplet = this.getTriplet();
    final Path libraryDirectory = root.resolve(LIBRARY_TREE).resolve(triplet);
    final Path legacyDirectory = root.resolve(LEGACY_LIBRARY_TREE).resolve(triplet);
    return List.of(libraryDirectory, legacyDirectory);
  }

  private List<Path> listDirectory(final Path directory) throws IOException {
    final boolean exists = Files.isDirectory(directory);
    if (!exists) {
      return List.of();
    }

    try (final Stream<Path> files = Files.list(directory)) {
      return files.toList();
    }
  }

  // the real files only: a symbolic link names a library that its target already loads
  private boolean isLibrary(final Path file) {
    final Path fileName = file.getFileName();
    final String name = String.valueOf(fileName);
    final boolean regular = Files.isRegularFile(file) && !Files.isSymbolicLink(file);
    return regular && name.startsWith(LIBRARY_PREFIX) && name.contains(LIBRARY_SUFFIX);
  }

  /**
   * Loads every library of the installation into the JVM, the libraries of each package after those of the packages
   * it depends on. A library that does not load is retried after the others, and skipped with a warning if it never
   * loads.
   *
   * @throws InstallationException if the packages are not installed, or the installation cannot be read
   * @since 1.0.0
   */
  public synchronized void loadLibraries() {
    final List<String> loadOrder = this.readLoadOrder();
    List<Path> pending = new ArrayList<>();
    for (final String library : loadOrder) {
      final Path path = this.resolveLibrary(library);
      pending.add(path);
    }

    int remaining = -1;
    while (pending.size() != remaining) {
      remaining = pending.size();
      pending = this.loadEach(pending);
    }

    for (final Path library : pending) {
      LOGGER.warn("Library {} of {} could not be loaded and is skipped.", library, this.name);
    }
  }

  private List<String> readLoadOrder() {
    final Path file = this.folder.resolve(LOAD_ORDER_FILE);
    try {
      return Files.readAllLines(file, StandardCharsets.UTF_8);
    } catch (final IOException exception) {
      final String message = "Installation of %s at %s could not be read!".formatted(this.name, this.folder);
      throw new InstallationException(message, exception);
    }
  }

  private Path resolveLibrary(final String library) {
    final Path resolved = this.folder.resolve(library).normalize();
    final boolean inside = resolved.startsWith(this.folder) && !resolved.equals(this.folder);
    if (!inside) {
      final String message = "Library %s of %s is outside its installation!".formatted(library, this.name);
      throw new InstallationException(message);
    }

    return resolved;
  }

  // Loads every library it can and returns the others.
  private List<Path> loadEach(final List<Path> libraries) {
    final List<Path> failed = new ArrayList<>();
    for (final Path library : libraries) {
      final boolean loaded = this.load(library);
      if (!loaded) {
        failed.add(library);
      }
    }

    return failed;
  }

  private boolean load(final Path library) {
    try {
      final String path = library.toString();
      this.libraryLoader.accept(path);
      return true;
    } catch (final UnsatisfiedLinkError error) {
      LOGGER.debug("Library {} could not be loaded yet.", library, error);
      return false;
    }
  }

  /**
   * Returns whether this platform has packages.
   *
   * @return whether {@link #download(FileMode)} can install the packages here
   * @since 1.0.0
   */
  @Override
  public boolean isSupported() {
    final List<Download> packages = this.findPackages();
    return !packages.isEmpty();
  }

  /**
   * Returns the library folder of an earlier installation, without downloading anything.
   *
   * @return the library folder, or empty if the packages are not installed
   * @since 1.0.0
   */
  @Override
  public Optional<Path> findInstallation() {
    final Path loadOrder = this.folder.resolve(LOAD_ORDER_FILE);
    final boolean installed = Files.isRegularFile(loadOrder);
    if (!installed) {
      return Optional.empty();
    }

    final Path libraryDirectory = this.getPath();
    return Optional.of(libraryDirectory);
  }

  /**
   * Returns the library folder of the installation, such as {@code usr/lib/aarch64-linux-gnu} inside the installation
   * folder, whether or not the packages are installed.
   *
   * @return the library folder
   * @since 1.0.0
   */
  @Override
  public Path getPath() {
    final List<Path> directories = this.getLibraryDirectories(this.folder);
    return directories.getFirst();
  }

  private String getTriplet() {
    final Arch arch = this.platform.getArch();
    if (arch == Arch.ARM) {
      return AARCH64_TRIPLET;
    }

    return X86_64_TRIPLET;
  }

  private Path getParent() {
    final Path absolute = this.folder.toAbsolutePath();
    final @Nullable Path parent = absolute.getParent();
    return Objects.requireNonNull(parent, "An installation folder inside the cache always has a parent!");
  }
}
