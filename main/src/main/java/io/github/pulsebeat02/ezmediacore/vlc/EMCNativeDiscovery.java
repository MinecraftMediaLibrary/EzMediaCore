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
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
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
  private final boolean heuristics;

  private Path path;

  public EMCNativeDiscovery(
      @NotNull final MediaLibraryCore core,
      @NotNull final NativeDiscoveryAlgorithm algorithm,
      final boolean heuristics) {
    this.core = core;
    this.algorithm = algorithm;
    this.type = core.getDiagnostics().getSystem().getOSType();
    final String extension = algorithm.getFileExtension();
    this.keyword = "libvlc.%s".formatted(extension);
    this.heuristics = heuristics;
  }

  @Override
  public @NotNull Optional<Path> discover(@NotNull final Path directory) throws IOException {

    if (this.path != null) {
      return Optional.of(this.path);
    }

    /*
    If NativeDiscovery from VLCJ built in finds the binary, use that
    instead.
     */
    final NativeDiscovery discovery = new NativeDiscovery();
    if (discovery.discover()) {
      return Optional.of(Path.of(discovery.discoveredPath()));
    }

    if (Files.notExists(directory)) {
      throw new IOException("Folder doesn't exist!");
    }

    final Queue<Path> files = this.getPriorityQueue();
    files.add(directory);

    boolean plugins = false;
    boolean libvlc = false;

    while (!files.isEmpty()) {

      if (plugins && libvlc) {
        return Optional.of(directory);
      }

      final Path seek = files.remove();
      final String name = PathUtils.getName(seek);

      if (Files.isDirectory(seek)) {
        if (!plugins && name.equals("plugins")) {
          for (final String pattern : this.algorithm.getSearchPatterns()) {
            final Path extended = seek.resolve(pattern);
            if (Files.exists(extended)) {
              Logger.info("Found VLC plugins path: %s".formatted(extended));
              this.setVLCPluginPath(extended);
              plugins = true;
            }
          }
        } else {
          try (final Stream<Path> stream = Files.walk(seek)) {
            stream
                .filter(
                    file ->
                        Files.isDirectory(file) || PathUtils.getName(file).endsWith(this.keyword))
                .filter(file -> !file.equals(seek))
                .forEach(files::add);
          }
        }
      } else {
        if (!libvlc && name.equals(this.keyword)) {

          this.path = seek.getParent();
          Logger.info("Found VLC LibVLC folder path: %s".formatted(this.path));

          NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), this.path.toString());
          this.algorithm.onLibVlcFound(this.path);

          if (this.loadLibVLCLibrary()) {
            Logger.info("Successfully loaded LibVLC library binary!");
          } else {
            throw new AssertionError(
                "Invalid VLC versions! Please contact the plugin developer for support.");
          }

          libvlc = true;
        }
      }
    }

    return Optional.empty();
  }

  /*
  Heuristic algorithm which allows the libvlc compiled file be found easier on Unix
  systems. (This includes Mac and Linux, where the file is deeply in the recursion).
  It is not needed for Windows as the file is in the main directory.
   */
  private PriorityQueue<Path> getPriorityQueue() {
    return this.heuristics
        ? new PriorityQueue<>()
        : new PriorityQueue<>(
            (o1, o2) -> {
              final String name = PathUtils.getName(o1);
              if (name.equals(this.keyword) || name.equals("lib")) {
                return Integer.MIN_VALUE;
              }
              return o1.compareTo(o2);
            });
  }

  private void setVLCPluginPath(@NotNull final Path path) {
    final String env = System.getenv("VLC_PLUGIN_PATH");
    final String pluginPath = "VLC_PLUGIN_PATH";
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
      final libvlc_instance_t instance = libvlc_new(0, new StringArray(new String[0]));
      if (instance != null) {
        libvlc_release(instance);
        final LibVlcVersion version = new LibVlcVersion();
        if (version.isSupported()) {
          return true;
        }
      }
    } catch (final UnsatisfiedLinkError e) {
      Logger.info(e.getMessage());
    }
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
  public boolean isHeuristics() {
    return this.heuristics;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
