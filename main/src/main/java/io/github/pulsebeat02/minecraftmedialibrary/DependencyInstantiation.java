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

package io.github.pulsebeat02.minecraftmedialibrary;

import io.github.pulsebeat02.minecraftmedialibrary.dependency.DependencyManagement;
import io.github.pulsebeat02.minecraftmedialibrary.dependency.FFmpegDependencyInstallation;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.utility.DependencyUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VLCUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.VLCNativeDependencyFetcher;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** A special dependency instantiation class used to run dependency tasks asynchronously. */
public final class DependencyInstantiation {

  private final MediaLibrary instance;

  /**
   * Instantiates a new DependencyInstantiation for loading dependencies.
   *
   * @param library the library
   */
  public DependencyInstantiation(@NotNull final MediaLibrary library) {
    instance = library;
  }

  /** Starts dependency tasks. */
  public void startTasks() {
    assignClassLoader();
    try {
      CompletableFuture.allOf(
              CompletableFuture.runAsync(this::loadFfmpeg),
              CompletableFuture.runAsync(this::loadDependencies).thenRunAsync(this::loadVLC))
          .get();
    } catch (final InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  /** Assigns ClassLoader for classpath loading. */
  private void assignClassLoader() {
    DependencyUtilities.setClassloader(
        (URLClassLoader) instance.getPlugin().getClass().getClassLoader());
  }

  /** Downloads/Loads Jave dependency. */
  private void loadFfmpeg() {
    final FFmpegDependencyInstallation ffmpegDependencyInstallation =
        new FFmpegDependencyInstallation(instance);
    ffmpegDependencyInstallation.install();
  }

  /** Downloads/Loads Jitpack/Maven dependencies. */
  private void loadDependencies() {
    final DependencyManagement dependencyManagement = new DependencyManagement(instance);
    dependencyManagement.install();
    dependencyManagement.relocate();
    dependencyManagement.load();
    dependencyManagement.delete();
  }

  /** Downloads/Loads VLC dependency. */
  private void loadVLC() {
    if (!VLCUtilities.checkVLCExistence(instance.getVlcFolder())) {
      if (RuntimeUtilities.isLinux()) {
        Logger.info(
            "Unfortunately, MinecraftMediaLibrary cannot download VLC binaries on Linux "
                + "as it requires the user to manually install them instead. A VLC Media Player installation "
                + "could not be found on this computer. Instead, the library will resort to using FFmpeg to "
                + "play videos.");
        instance.setVlcj(false);
      } else {
        Logger.info(
            "Windows and Mac computers are supported by VLC. Proceeding to download the libraries for VLC.");
        new VLCNativeDependencyFetcher(instance).downloadLibraries();
      }
    }
  }
}
