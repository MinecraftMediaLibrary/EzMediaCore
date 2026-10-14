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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import me.brandonli.mcav.capability.installer.Download;
import me.brandonli.mcav.capability.installer.FileMode;
import me.brandonli.mcav.utils.IoUtils;
import me.brandonli.mcav.utils.os.Arch;
import me.brandonli.mcav.utils.os.Bits;
import me.brandonli.mcav.utils.os.Os;
import me.brandonli.mcav.utils.os.Platform;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// Installs the bundled Debian package lists from the real snapshot archive, which checks every pinned hash and that
// the closures extract. These tests download about 130 MiB, so they only run when the mcav.networkTests system
// property is true, which ./gradlew test -Pmcav.networkTests=true sets. Only the libraries of this machine's
// architecture are loaded; the 64-bit ARM VLC is only installed.
final class DebianLibraryReleaseTest {

  private static final String VLC_RESOURCE = "mcav/common/installers/debian-vlc.json";
  private static final String GTK2_RESOURCE = "mcav/common/installers/debian-gtk2.json";

  private static final Platform ARM64 = Platform.ofPlatform(Os.LINUX, Arch.ARM, Bits.BITS_64);
  private static final Platform X86_64 = Platform.ofPlatform(Os.LINUX, Arch.X86, Bits.BITS_64);

  private static final String LOAD_ORDER_FILE = "load-order.txt";
  private static final String GTK2_LIBRARY = "libgtk-x11-2.0.so.0";

  @TempDir
  private Path temporary;

  @BeforeAll
  static void requireNetworkTests() {
    final boolean enabled = Boolean.getBoolean("mcav.networkTests");
    Assumptions.assumeTrue(enabled, "Network tests run only with -Pmcav.networkTests=true!");
  }

  @Test
  void shouldInstallVlcForArm64WithItsPluginsAfterItsDependencies() throws IOException {
    final DebianLibraryInstaller installer = this.createInstaller(VLC_RESOURCE, ARM64, this::refuse);
    final Path libraryDirectory = installer.download(FileMode.EXECUTABLE);

    final boolean libvlc = Files.exists(libraryDirectory.resolve("libvlc.so.5"));
    final boolean plugins = Files.isDirectory(libraryDirectory.resolve("vlc/plugins"));
    assertTrue(libvlc);
    assertTrue(plugins);

    final List<String> loadOrder = this.readLoadOrder();
    final int core = this.indexOf(loadOrder, "/libvlccore.so.9");
    final int vlc = this.indexOf(loadOrder, "/libvlc.so.5");
    assertTrue(core >= 0 && core < vlc, "Libvlccore must load before libvlc!");
  }

  @Test
  void shouldInstallGtk2ForArm64() throws IOException {
    final DebianLibraryInstaller installer = this.createInstaller(GTK2_RESOURCE, ARM64, this::refuse);
    final Path libraryDirectory = installer.download(FileMode.EXECUTABLE);

    final boolean gtk2 = Files.exists(libraryDirectory.resolve(GTK2_LIBRARY));
    assertTrue(gtk2);
  }

  @Test
  void shouldInstallAndLoadEveryGtk2LibraryOnThisMachine() throws IOException {
    final Platform current = Platform.getCurrentPlatform();
    Assumptions.assumeTrue(current.equals(X86_64), "Only a 64-bit x86 Linux machine loads the x86-64 libraries!");

    final List<String> loaded = new ArrayList<>();
    final DebianLibraryInstaller installer = this.createInstaller(GTK2_RESOURCE, X86_64, library -> this.loadAndRecord(library, loaded));
    installer.download(FileMode.EXECUTABLE);
    installer.loadLibraries();

    final List<String> loadOrder = this.readLoadOrder();
    assertEquals(loadOrder.size(), loaded.size(), "Every library must load!");

    final boolean gtk2 = loaded.stream().anyMatch(library -> library.contains(GTK2_LIBRARY));
    assertTrue(gtk2);
  }

  private DebianLibraryInstaller createInstaller(final String resource, final Platform platform, final Consumer<String> loader) {
    final Download[] downloads = IoUtils.readDownloadsFromJsonResource(resource);
    final Download[] platformDownloads = Arrays.stream(downloads)
      .filter(download -> download.getPlatform().equals(platform))
      .toArray(Download[]::new);
    final Path folder = this.temporary.resolve("installation");
    return new DebianLibraryInstaller(folder, "debian-release", platformDownloads, platform, loader);
  }

  private List<String> readLoadOrder() throws IOException {
    final Path folder = this.temporary.resolve("installation");
    return Files.readAllLines(folder.resolve(LOAD_ORDER_FILE));
  }

  private int indexOf(final List<String> loadOrder, final String library) {
    for (int index = 0; index < loadOrder.size(); index++) {
      if (loadOrder.get(index).contains(library)) {
        return index;
      }
    }

    return -1;
  }

  private void refuse(final String library) {
    throw new UnsatisfiedLinkError("Libraries of another architecture are not loaded!");
  }

  // the libraries of this machine's architecture really load, which proves that the dependency order holds
  @SuppressWarnings("restricted")
  private void loadAndRecord(final String library, final List<String> loaded) {
    System.load(library);
    loaded.add(library);
  }
}
