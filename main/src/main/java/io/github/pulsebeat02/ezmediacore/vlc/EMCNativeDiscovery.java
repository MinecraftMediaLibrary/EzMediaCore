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
package io.github.pulsebeat02.ezmediacore.vlc;

import static uk.co.caprica.vlcj.binding.LibVlc.libvlc_new;
import static uk.co.caprica.vlcj.binding.LibVlc.libvlc_release;

import com.sun.jna.NativeLibrary;
import com.sun.jna.StringArray;
import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import io.github.pulsebeat02.ezmediacore.vlc.os.DiscoveryProvider;
import io.github.pulsebeat02.ezmediacore.vlc.os.NativeDiscoveryAlgorithm;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.LibC;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.binding.internal.libvlc_instance_t;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;

public class EMCNativeDiscovery implements DiscoveryProvider {

  private final MediaLibraryCore core;
  private final NativeDiscoveryAlgorithm algorithm;
  private final OSType type;
  private final String keyword;

  public EMCNativeDiscovery(
      @NotNull final MediaLibraryCore core, @NotNull final NativeDiscoveryAlgorithm algorithm) {
    this.core = core;
    this.algorithm = algorithm;
    this.type = core.getDiagnostics().getSystem().getOSType();
    this.keyword = "libvlc.%s".formatted(algorithm.getFileExtension());
  }

  @Override
  public @NotNull Optional<Path> discover(@NotNull final Path directory) {

    Logger.info("Searching in Directory %s".formatted(directory));

    final NativeDiscovery discovery = new NativeDiscovery();
    if (discovery.discover()) {
      return Optional.of(Path.of(discovery.discoveredPath()));
    }

    if (Files.notExists(directory)) {
      return Optional.empty();
    }

    try {
      final CompletableFuture<Boolean> plugins =
          CompletableFuture.supplyAsync(() -> this.locatePluginsFolder(directory));
      final CompletableFuture<Boolean> libvlc =
          CompletableFuture.supplyAsync(() -> this.locateLibVLC(directory));
      if (plugins.get() && libvlc.get()) {
        this.loadVLC();
        return Optional.of(directory);
      }
    } catch (final InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  private boolean locatePluginsFolder(@NotNull final Path directory) {
    try (final Stream<Path> stream = Files.walk(directory).parallel()) {
      final Optional<Path> plugins =
          stream.filter(path -> PathUtils.getName(path).equals("plugins")).findFirst();
      if (plugins.isPresent()) {
        final Path path = plugins.get();
        for (final String pattern : this.algorithm.getSearchPatterns()) {
          final Path extended = path.resolve(pattern);
          if (Files.exists(extended)) {
            Logger.info("Found VLC Plugins path at %s".formatted(extended));
            this.setVLCPluginPath(extended);
            return true;
          }
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private boolean locateLibVLC(@NotNull final Path directory) {
    try (final Stream<Path> stream = Files.walk(directory).parallel()) {
      final String keyword = this.getKeyword();
      final Optional<Path> libvlc =
          stream.filter(path -> PathUtils.getName(path).equals(keyword)).findFirst();
      if (libvlc.isPresent()) {
        final Path path = libvlc.get().getParent();
        Logger.info("Found VLC LibVLC folder path: %s".formatted(path));
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), path.toString());
        this.algorithm.onLibVlcFound(path);
        return true;
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private void loadVLC() {
    if (this.loadLibVLCLibrary()) {
      Logger.info("Successfully loaded LibVLC library binary!");
    } else {
      Logger.info("Invalid VLC versions! Please contact the plugin developer for support.");
    }
  }

  private void setVLCPluginPath(@NotNull final Path path) {
    final String pluginPath = "VLC_PLUGIN_PATH";
    final String env = System.getenv(pluginPath);
    if (env == null || env.length() == 0) {
      if (this.type == OSType.WINDOWS) {
        LibC.INSTANCE._putenv("%s=%s".formatted(pluginPath, path));
      } else {
        LibC.INSTANCE.setenv(pluginPath, path.toString(), 1);
      }
    }
  }

  private boolean loadLibVLCLibrary() {
    try {
      final libvlc_instance_t instance =
          libvlc_new(0, new StringArray(new String[]{"--reset-plugins-cache"}));
      if (instance != null) {
        libvlc_release(instance);
        final LibVlcVersion version = new LibVlcVersion();
        Logger.info("LibVLC Version: %s".formatted(version.getVersion()));
        if (version.isSupported()) {
          return true;
        }
      } else {
        Logger.error("Could not load libvlc_instance_t! (null)");
      }
    } catch (final UnsatisfiedLinkError e) {
      e.printStackTrace();
    }
    return false;
  }

  private boolean resetPluginCache() {
    final Calendar c = new GregorianCalendar();
    c.setTime(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
    if (c.get(Calendar.DAY_OF_MONTH) == 1) {
      Logger.info("Resetting plugin cache! (Monthly reset)");
      return true;
    }
    Logger.info("Plugin cache up to date!");
    return false;
  }

  @Override
  public @NotNull NativeDiscoveryAlgorithm getAlgorithm() {
    return this.algorithm;
  }

  @Override
  public @NotNull String getKeyword() {
    return this.keyword;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
