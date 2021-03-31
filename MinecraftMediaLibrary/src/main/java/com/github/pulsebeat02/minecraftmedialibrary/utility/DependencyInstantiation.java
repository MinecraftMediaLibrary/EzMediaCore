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

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.DependencyManagement;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.JaveDependencyInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.VLCNativeDependencyFetcher;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

import java.net.URLClassLoader;
import java.util.concurrent.CompletableFuture;

/** A special dependency instantiation class used to run dependency tasks asynchronously. */
public final class DependencyInstantiation {

  private final MinecraftMediaLibrary instance;

  /**
   * Instantiates a new DependencyInstantiation for loading dependencies.
   *
   * @param library the library
   */
  public DependencyInstantiation(@NotNull final MinecraftMediaLibrary library) {
    instance = library;
  }

  /** Starts dependency tasks. */
  public void startTasks() {
    assignClassLoader();
    loadJave();
    loadDependencies();
    if (!DependencyUtilities.vlcExists(instance)) {
      loadVLC();
    }
  }

  /** Assigns ClassLoader for classpath loading. */
  public void assignClassLoader() {
    DependencyUtilities.CLASSLOADER =
        (URLClassLoader) instance.getPlugin().getClass().getClassLoader();
  }

  /** Downloads/Loads Jave dependency. */
  public void loadJave() {
    final JaveDependencyInstallation javeDependencyInstallation =
        new JaveDependencyInstallation(instance);
    javeDependencyInstallation.install();
    javeDependencyInstallation.load();
  }

  /** Downloads/Loads Jitpack/Maven dependencies. */
  public void loadDependencies() {
    final DependencyManagement dependencyManagement = new DependencyManagement(instance);
    dependencyManagement.install();
    dependencyManagement.relocate();
    dependencyManagement.load();
  }

  /** Downloads/Loads VLC dependency. */
  public void loadVLC() {
    new VLCNativeDependencyFetcher(instance).downloadLibraries();
    if (instance.isUsingVLCJ()) {
      try {
        new MediaPlayerFactory();
      } catch (final Exception e) {
        Logger.error("The user does not have VLCJ installed! This is a very fatal error.");
        instance.setVlcj(false);
        e.printStackTrace();
      }
    }
  }
}
