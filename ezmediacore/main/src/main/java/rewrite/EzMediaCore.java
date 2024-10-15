/*
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
package rewrite;

import rewrite.capabilities.SelectCapability;
import rewrite.dependency.DependencyLoader;
import rewrite.dither.load.ColorPalette;
import rewrite.listener.RegistrationListener;
import rewrite.logging.LibraryLogger;
import rewrite.logging.Logger;
import rewrite.reflect.PacketToolsProvider;
import rewrite.util.io.FileUtils;

import java.io.File;
import java.nio.file.Path;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class EzMediaCore {

  /*

  - Play videos with shaders on https://hangar.papermc.io/JNNGL/VanillaMinimaps
  - Rewrite entire library (resourcepack package)
  - Add other color palettes
  - Add more dithering algorithms

   */

  private final Plugin plugin;

  private Path libraryPath;
  private Path httpServerPath;
  private Path dependencyPath;
  private Path imagePath;
  private Logger logger;
  private Listener registrationListener;

  public EzMediaCore(final Plugin plugin) {
    this(plugin, SelectCapability.DEFAULT_CAPABILITIES);
  }

  public EzMediaCore(final Plugin plugin, final SelectCapability... capabilities) {
    this.plugin = plugin;
    this.assignPaths(plugin);
    this.initLogger();
    this.cacheLookups();
    this.loadDependencies(capabilities);
    this.registerEvents();
    this.createFolders();
  }

  private void assignPaths(final Plugin plugin) {
    final File data = plugin.getDataFolder();
    final Path parent = data.toPath();
    this.libraryPath = parent.resolve("ezmediacore");
    this.dependencyPath =  parent.resolve("dependencies");
    this.httpServerPath = parent.resolve("server");
    this.imagePath = parent.resolve("image");
  }

  private void initLogger() {
    this.logger = new LibraryLogger(this);
    this.logger.start();
  }

  private void cacheLookups() {
    ColorPalette.init();
    PacketToolsProvider.init();
  }

  private void loadDependencies(final SelectCapability[] capabilities) {
    final DependencyLoader loader = new DependencyLoader(this, capabilities);
    loader.start();
  }

  private void registerEvents() {
    this.registrationListener = new RegistrationListener(this);
  }

  private void createFolders() {
    FileUtils.createDirectoryIfNotExistsExceptionally(this.libraryPath);
    FileUtils.createDirectoryIfNotExistsExceptionally(this.dependencyPath);
    FileUtils.createDirectoryIfNotExistsExceptionally(this.httpServerPath);
    FileUtils.createDirectoryIfNotExistsExceptionally(this.imagePath);
  }

  public void shutdown() {
    HandlerList.unregisterAll(this.registrationListener);
    this.logger.release();
  }

  public Plugin getPlugin() {
    return this.plugin;
  }

  public Path getLibraryPath() {
    return this.libraryPath;
  }

  public Path getHttpServerPath() {
    return this.httpServerPath;
  }

  public Path getDependencyPath() {
    return this.dependencyPath;
  }

  public Path getImagePath() {
    return this.imagePath;
  }

  public Logger getLogger() {
    return this.logger;
  }
}
