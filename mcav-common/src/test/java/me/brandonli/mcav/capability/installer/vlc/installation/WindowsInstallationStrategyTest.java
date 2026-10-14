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
package me.brandonli.mcav.capability.installer.vlc.installation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import me.brandonli.mcav.capability.installer.vlc.VlcInstaller;
import me.brandonli.mcav.utils.ZipEntryIntegrityException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class WindowsInstallationStrategyTest {

  private static final String TEMPORARY_DIRECTORY = "vlc-extract";

  private static final String ARCHIVE_NAME = "vlc.zip";
  private static final String PORTABLE_ARCHIVE_NAME = "vlc-3.0.24-win64.zip";

  private static final String ROOT_DIRECTORY = "vlc-3.0.24";
  private static final String LIBRARY_ENTRY = "vlc-3.0.24/libvlc.dll";
  private static final String CORE_LIBRARY_ENTRY = "vlc-3.0.24/libvlccore.dll";
  private static final String PLUGIN_ENTRY = "vlc-3.0.24/plugins/codec/libavcodec_plugin.dll";
  private static final String README_ENTRY = "vlc-3.0.24/readme.txt";
  private static final String ESCAPING_ENTRY = "../escaped.dll";

  private static final String LIBRARY_FILE = "libvlc.dll";
  private static final String CORE_LIBRARY_FILE = "libvlccore.dll";
  private static final String PLUGIN_DIRECTORY = "plugins";
  private static final String CODEC_DIRECTORY = "codec";
  private static final String PLUGIN_FILE = "libavcodec_plugin.dll";
  private static final String STALE_FILE = "stale.dll";
  private static final String ESCAPED_FILE = "escaped.dll";

  @TempDir
  private Path temporaryDirectory;

  @Test
  void shouldExtractTheInnerDirectoryIntoTheInstallationDirectory() throws IOException {
    final VlcInstaller installer = VlcInstaller.create(this.temporaryDirectory);
    final WindowsInstallationStrategy strategy = new WindowsInstallationStrategy(installer);
    final Path archive = this.createZip(PORTABLE_ARCHIVE_NAME, LIBRARY_ENTRY, CORE_LIBRARY_ENTRY, PLUGIN_ENTRY);
    final Path installed = strategy.execute(archive);
    final Path installDirectory = installer.getInstallDirectory();
    final boolean hasLibrary = containsFile(installed, LIBRARY_FILE);
    final boolean hasCore = containsFile(installed, CORE_LIBRARY_FILE);
    final boolean hasPlugin = containsFile(installed, PLUGIN_DIRECTORY, CODEC_DIRECTORY, PLUGIN_FILE);
    final boolean archiveExists = Files.exists(archive);
    final boolean temporaryExists = this.temporaryDirectoryExists();
    assertEquals(installDirectory, installed);
    assertTrue(hasLibrary);
    assertTrue(hasCore);
    assertTrue(hasPlugin);
    assertFalse(archiveExists);
    assertFalse(temporaryExists);
  }

  @Test
  void shouldNotPromoteStaleFilesFromAnEarlierExtraction() throws IOException {
    final VlcInstaller installer = VlcInstaller.create(this.temporaryDirectory);
    final WindowsInstallationStrategy strategy = new WindowsInstallationStrategy(installer);
    final Path temporary = this.temporaryDirectory.resolve(TEMPORARY_DIRECTORY);
    final Path previousRoot = temporary.resolve(ROOT_DIRECTORY);
    Files.createDirectories(previousRoot);
    final Path stale = previousRoot.resolve(STALE_FILE);
    Files.writeString(stale, "left over from an interrupted extraction");
    final Path archive = this.createZip(ARCHIVE_NAME, LIBRARY_ENTRY, CORE_LIBRARY_ENTRY);
    final Path installed = strategy.execute(archive);
    final Path relocatedStale = installed.resolve(STALE_FILE);
    final boolean copied = Files.exists(relocatedStale);
    assertFalse(copied, "only files from the new archive may enter the installation");
  }

  @Test
  void shouldReportTheInstallationOnlyOnceItExists() throws IOException {
    final VlcInstaller installer = VlcInstaller.create(this.temporaryDirectory);
    final WindowsInstallationStrategy strategy = new WindowsInstallationStrategy(installer);
    final Optional<Path> before = strategy.getInstalledPath();
    final Path archive = this.createZip(ARCHIVE_NAME, LIBRARY_ENTRY, CORE_LIBRARY_ENTRY);
    final Path installed = strategy.execute(archive);
    final Optional<Path> after = strategy.getInstalledPath();
    final Optional<Path> expected = Optional.of(installed);
    final boolean nothingBefore = before.isEmpty();
    assertTrue(nothingBefore);
    assertEquals(expected, after);
  }

  @Test
  void shouldReplaceAnEarlierInstallation() throws IOException {
    final VlcInstaller installer = VlcInstaller.create(this.temporaryDirectory);
    final WindowsInstallationStrategy strategy = new WindowsInstallationStrategy(installer);
    final Path installDirectory = installer.getInstallDirectory();
    Files.createDirectories(installDirectory);
    final Path stale = installDirectory.resolve(STALE_FILE);
    Files.createFile(stale);
    final Path archive = this.createZip(ARCHIVE_NAME, LIBRARY_ENTRY, CORE_LIBRARY_ENTRY);
    strategy.execute(archive);
    final boolean staleExists = Files.exists(stale);
    final boolean libraryExists = containsFile(installDirectory, LIBRARY_FILE);
    assertFalse(staleExists);
    assertTrue(libraryExists);
  }

  @Test
  void shouldFailAndCleanUpWhenTheZipHasNoLibrary() throws IOException {
    final VlcInstaller installer = VlcInstaller.create(this.temporaryDirectory);
    final WindowsInstallationStrategy strategy = new WindowsInstallationStrategy(installer);
    final Path archive = this.createZip(ARCHIVE_NAME, README_ENTRY);
    final IOException thrown = assertThrows(IOException.class, () -> strategy.execute(archive));
    final String message = thrown.getMessage();
    final boolean namesTheLibrary = message.contains(LIBRARY_FILE);
    final boolean temporaryExists = this.temporaryDirectoryExists();
    final Path installDirectory = installer.getInstallDirectory();
    final boolean installExists = Files.exists(installDirectory);
    assertTrue(namesTheLibrary, message);
    assertFalse(temporaryExists);
    assertFalse(installExists);
  }

  @Test
  void shouldReportAFileThatIsNotAZipAndCleanUp() throws IOException {
    final VlcInstaller installer = VlcInstaller.create(this.temporaryDirectory);
    final WindowsInstallationStrategy strategy = new WindowsInstallationStrategy(installer);
    final Path archive = this.temporaryDirectory.resolve(ARCHIVE_NAME);
    Files.writeString(archive, "this is not a zip", StandardCharsets.US_ASCII);
    final IOException thrown = assertThrows(IOException.class, () -> strategy.execute(archive));
    final Throwable cause = thrown.getCause();
    final UncheckedIOException unzipFailure = assertInstanceOf(UncheckedIOException.class, cause);
    final Throwable zipFailure = unzipFailure.getCause();
    final boolean temporaryExists = this.temporaryDirectoryExists();
    assertInstanceOf(ZipException.class, zipFailure, "the file is rejected as no zip, not read as an empty one");
    assertFalse(temporaryExists);
  }

  @Test
  void shouldReportUnsafeZipEntriesAsAnInstallationFailure() throws IOException {
    final VlcInstaller installer = VlcInstaller.create(this.temporaryDirectory);
    final WindowsInstallationStrategy strategy = new WindowsInstallationStrategy(installer);
    final Path archive = this.createZip(ARCHIVE_NAME, ESCAPING_ENTRY);
    final IOException thrown = assertThrows(IOException.class, () -> strategy.execute(archive));
    final Throwable cause = thrown.getCause();
    final Path escaped = this.temporaryDirectory.resolve(ESCAPED_FILE);
    final boolean escapedExists = Files.exists(escaped);
    final boolean temporaryExists = this.temporaryDirectoryExists();
    assertInstanceOf(ZipEntryIntegrityException.class, cause);
    assertFalse(escapedExists);
    assertFalse(temporaryExists);
  }

  @Test
  void shouldRejectANullArchive() {
    final VlcInstaller installer = VlcInstaller.create(this.temporaryDirectory);
    final WindowsInstallationStrategy strategy = new WindowsInstallationStrategy(installer);
    assertThrows(NullPointerException.class, () -> strategy.execute(null));
  }

  private boolean temporaryDirectoryExists() {
    final Path temporary = this.temporaryDirectory.resolve(TEMPORARY_DIRECTORY);
    return Files.exists(temporary);
  }

  private static boolean containsFile(final Path directory, final String first, final String... more) {
    final Path relative = Path.of(first, more);
    final Path file = directory.resolve(relative);
    return Files.isRegularFile(file);
  }

  private Path createZip(final String name, final String... entries) throws IOException {
    final Path zip = this.temporaryDirectory.resolve(name);
    try (final OutputStream file = Files.newOutputStream(zip); final ZipOutputStream zipOutput = new ZipOutputStream(file)) {
      for (final String entry : entries) {
        final ZipEntry zipEntry = new ZipEntry(entry);
        zipOutput.putNextEntry(zipEntry);
        final byte[] content = entry.getBytes(StandardCharsets.UTF_8);
        zipOutput.write(content);
        zipOutput.closeEntry();
      }
    }

    return zip;
  }
}
