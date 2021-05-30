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

package io.github.pulsebeat02.minecraftmedialibrary.vlc.os;

import com.sun.jna.NativeLibrary;
import com.sun.jna.StringArray;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.LibC;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.binding.internal.libvlc_instance_t;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import static uk.co.caprica.vlcj.binding.LibVlc.libvlc_new;
import static uk.co.caprica.vlcj.binding.LibVlc.libvlc_release;

public class MMLNativeDiscovery {

  private final boolean heuristics;
  private final String extension;
  private final String keyword;
  private final Set<String> possibleExtensions;
  private Path nativeVLCPath;

  public MMLNativeDiscovery(
      final boolean heuristics,
      @NotNull final String extension,
      @NotNull final Set<String> possibleExtensions) {
    this.heuristics = heuristics;
    this.extension = extension;
    keyword = String.format("libvlc.%s", extension);
    this.possibleExtensions = possibleExtensions;
    final NativeDiscovery discovery = new NativeDiscovery();
    if (discovery.discover()) {
      nativeVLCPath = Paths.get(discovery.discoveredPath());
    }
  }

  /**
   * Checks if VLC installation exists or not.
   *
   * @param directory the library
   * @return whether vlc can be found or not
   */
  public boolean discover(@NotNull final Path directory) {
    if (!Files.exists(directory)) {
      return false;
    }
    if (nativeVLCPath != null) {
      return true;
    }
    boolean plugins = false;
    boolean libvlc = false;
    final Queue<Path> folders = getPriorityQueue(keyword);
    folders.add(directory);
    while (!folders.isEmpty()) {
      if (plugins && libvlc) {
        return true;
      }
      final Path f = folders.remove();
      final String name = PathUtilities.getName(f);
      final String path = f.toAbsolutePath().toString();
      if (Files.isDirectory(f)) {
        if (!plugins && name.equals("plugins")) {
          for (final String extension : possibleExtensions) {
            final String pathExtension = String.format("%s%s", f.getParent(), extension);
            if (Files.exists(Paths.get(pathExtension))) {
              setVLCPluginPath(pathExtension);
              Logger.info(String.format("Found Plugins Path (%s)", path));
              plugins = true;
            }
          }
        } else {
          try (final Stream<Path> stream = Files.walk(f)) {
            stream
                .filter(x -> Files.isDirectory(x) || PathUtilities.getName(x).endsWith(extension))
                .filter(x -> !x.equals(f))
                .forEach(folders::add);
          } catch (final IOException e) {
            e.printStackTrace();
          }
        }
      } else {
        if (!libvlc && name.equals(keyword)) {
          nativeVLCPath = f.getParent().toAbsolutePath();
          final String vlcPath = nativeVLCPath.toAbsolutePath().toString();
          setupVLC();
          NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath);
          Logger.info(String.format("Found LibVLC (%s)", path));
          if (loadLibVLCLibrary()) {
            Logger.info("Successfully Loaded LibVLC Library");
          }
          libvlc = true;
        }
      }
    }
    return false;
  }

  public void setupVLC() {}

  private PriorityQueue<Path> getPriorityQueue(@NotNull final String keyword) {
    return heuristics
        ? new PriorityQueue<>()
        : new PriorityQueue<>(
            (o1, o2) -> {

              /*

              Heuristic algorithm which allows the libvlc compiled file be found easier on Unix
              systems. (This includes Mac and Linux, where the file is deeply in the recursion).
              It is not needed for Windows as the file is in the main directory.

               */

              final String name = PathUtilities.getName(o1);
              if (name.equals(keyword) || name.equals("lib")) {
                return Integer.MIN_VALUE;
              }
              return o1.compareTo(o2);
            });
  }

  /**
   * Sets the VLC plugin path to the specified path provided.
   *
   * @param path the vlc plugin path
   */
  private void setVLCPluginPath(@NotNull final String path) {
    final String env = System.getenv("VLC_PLUGIN_PATH");
    final String pluginPath = "VLC_PLUGIN_PATH";
    if (env == null || env.length() == 0) {
      if (RuntimeUtilities.isWindows()) {
        LibC.INSTANCE._putenv(String.format("%s=%s", pluginPath, path));
      } else {
        LibC.INSTANCE.setenv(pluginPath, path, 1);
      }
    }
  }

  /**
   * Loads the LibVLC library of VLC.
   *
   * @return whether if the library was successfully loaded or not.
   */
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

  /**
   * Gets the native path of VLC binaries.
   *
   * @return the File of the folder
   */
  public Path getDiscoveredPath() {
    return nativeVLCPath;
  }

  /**
   * Gets whether the data structure uses heuristics.
   *
   * @return whether the data structure uses heuristics or not
   */
  public boolean isHeuristics() {
    return heuristics;
  }

  /**
   * Gets the extension of the libvlc file.
   *
   * @return the extension of the libvlc file
   */
  public String getExtension() {
    return extension;
  }

  /**
   * Gets the file keyword (libvlc) to be searched within the files.
   *
   * @return the file keyword (libvlc) to be searched
   */
  public String getKeyword() {
    return keyword;
  }

  /**
   * Gets the parent plugin extension path appended to the co-existing path.
   *
   * @return the parent plugin extension path
   */
  public Set<String> getPossibleExtensions() {
    return possibleExtensions;
  }
}
