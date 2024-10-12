/**
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.emcinstallers.implementation;

import static io.github.pulsebeat02.emcinstallers.OS.WINDOWS;
import static io.github.pulsebeat02.emcinstallers.OS.getExecutablePath;
import static io.github.pulsebeat02.emcinstallers.OS.getOperatingSystem;
import static io.github.pulsebeat02.emcinstallers.OS.isArm;
import static io.github.pulsebeat02.emcinstallers.OS.isBits64;

import com.google.common.collect.Table;
import io.github.pulsebeat02.emcinstallers.OS;
import io.github.pulsebeat02.emcinstallers.implementation.Installer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

/** Base installer of all installers. */
public abstract class BaseInstaller implements Installer {

  private final Table<OS, Boolean, String> bits32;
  private final Table<OS, Boolean, String> bits64;
  private final String url;
  private Path path;

  /**
   * Creates a new BaseInstaller.
   *
   * @param folder the folder to target
   * @param name the name
   * @param bits32 installation links for 32-bit
   * @param bits64 installation links for 64-bit
   */
  public BaseInstaller(
      final Path folder,
      final String name,
      final Table<OS, Boolean, String> bits32,
      final Table<OS, Boolean, String> bits64) {
    this.path = folder.resolve(name);
    this.bits32 = bits32;
    this.bits64 = bits64;
    this.url = this.getDownloadUrl();
    if (this.url == null) {
      throw new AssertionError("Your current operating system is not supported!");
    }
    this.createFiles();
  }

  /**
   * Creates a new BaseInstaller.
   *
   * @param name the name
   * @param bits32 installation links for 32-bit
   * @param bits64 installation links for 64-bit
   */
  public BaseInstaller(
      final String name,
      final Table<OS, Boolean, String> bits32,
      final Table<OS, Boolean, String> bits64) {
    this(getExecutablePath(), name, bits32, bits64);
  }

  private String getDownloadUrl() {
    final OS os = getOperatingSystem();
    final boolean arm = isArm();
    return isBits64() ? this.bits64.get(os, arm) : this.bits32.get(os, arm);
  }

  private String getFilename() {
    return this.url.substring(this.url.lastIndexOf('/') + 1);
  }

  private void createFiles() {
    if (Files.notExists(this.path)) {
      this.createParentDirectories();
    }
  }

  private void createParentDirectories() {
    try {
      Files.createDirectories(this.path.getParent());
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Path download(final boolean chmod) throws IOException {

    if (this.checkExistingFile()) {
      return this.path;
    }

    this.downloadFile();
    this.changePermissions(chmod);
    this.renameFile();

    return this.path;
  }

  private void changePermissions(final boolean chmod) throws IOException {
    if (chmod) {
      this.changePermissions();
    }
  }

  private void downloadFile() throws IOException {
    try (final ReadableByteChannel readableByteChannel =
            Channels.newChannel(new URL(this.url).openStream());
        final FileOutputStream stream = new FileOutputStream(this.path.toFile());
        final FileChannel channel = stream.getChannel()) {
      channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }
  }

  private boolean checkExistingFile() {
    return Files.exists(this.path);
  }

  private void renameFile() throws IOException {
    if (getOperatingSystem() == WINDOWS) {
      final Path newPath = this.path.resolveSibling(String.format("%s.exe", this.getFilename()));
      Files.move(this.path, newPath);
      this.path = newPath;
    }
  }

  private void changePermissions() throws IOException {
    if (getOperatingSystem() != WINDOWS) {
      final ProcessBuilder builder = new ProcessBuilder("chmod", "777", this.path.toString());
      this.startProcess(builder);
    }
  }

  private void startProcess(final ProcessBuilder builder) throws IOException {
    try {
      builder.start().waitFor();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

  public String getUrl() {
    return this.url;
  }

  public Table<OS, Boolean, String> getBits32() {
    return this.bits32;
  }

  public Table<OS, Boolean, String> getBits64() {
    return this.bits64;
  }

  public Path getPath() {
    return this.path;
  }
}
