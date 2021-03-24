/*............................................................................................
 . Copyright © 2021 PulseBeat_02                                                             .
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

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.sun.jna.NativeLibrary;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * An abstract class used to install and load VLC silently into the user. It depends for each
 * operating system, however, this class is used to handle certain broilerplate code in an easier
 * fashion.
 */
public abstract class SilentOSDependentSolution {

  private final String dir;
  private final NativeDiscovery nativeDiscovery;

  /**
   * Instantiates a new SilentOSDependentSolution.
   *
   * @param library the library
   */
  public SilentOSDependentSolution(@NotNull final MinecraftMediaLibrary library) {
    dir = library.getVlcFolder();
    nativeDiscovery = new NativeDiscovery();
  }

  /**
   * Instantiates a new SilentOSDependentSolution.
   *
   * @param dir the directory
   */
  public SilentOSDependentSolution(@NotNull final String dir) {
    this.dir = dir;
    nativeDiscovery = new NativeDiscovery();
  }

  /**
   * Downloads VLC dependencies.
   *
   * @throws IOException if an issue occured during installation
   */
  public abstract void downloadVLCLibrary() throws IOException;

  /**
   * Checks existence of VLC folder.
   *
   * @param dir the directory
   * @return whether it can be found
   */
  public boolean checkVLCExistance(@NotNull final String dir) {
    final File folder = findVLCFolder(new File(dir));
    if (folder == null || !folder.exists()) {
      return false;
    }
    loadNativeDependency(folder);
    return getNativeDiscovery().discover();
  }

  /**
   * Loads native dependency from file.
   *
   * @param folder directory
   */
  public void loadNativeDependency(@NotNull final File folder) {
    NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), folder.getAbsolutePath());
    nativeDiscovery.discover();
  }

  /** Prints all System environment variables. */
  public void printSystemEnvironmentVariables() {
    Logger.info("======== SYSTEM ENVIRONMENT VARIABLES ========");
    for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
      Logger.info("Key: " + entry.getKey() + "| Entry: " + entry.getValue());
    }
    Logger.info("==============================================");
  }

  /** Prints all System properties. */
  public void printSystemProperties() {
    Logger.info("============== SYSTEM PROPERTIES ==============");
    final Properties p = System.getProperties();
    final Enumeration<Object> keys = p.keys();
    while (keys.hasMoreElements()) {
      final String key = (String) keys.nextElement();
      Logger.info("Key: " + key + "| Entry: " + p.get(key));
    }
    Logger.info("===============================================");
  }

  /**
   * Gets VLC folder in folder.
   *
   * @param folder search folder
   * @return file
   */
  public File findVLCFolder(@NotNull final File folder) {
    for (final File f : folder.listFiles()) {
      final String name = f.getName();
      if (StringUtils.containsIgnoreCase(name, "vlc") && !name.endsWith(".dmg")) {
        return f;
      }
    }
    return null;
  }

  /**
   * Deletes file (archive).
   *
   * @param zip archive
   */
  public void deleteArchive(@NotNull final File zip) {
    Logger.info("Deleting Archive...");
    if (zip.delete()) {
      Logger.info("Archive deleted after installation.");
    } else {
      Logger.error("Archive could NOT be deleted after installation!");
    }
  }

  /**
   * Gets directory of file.
   *
   * @return directory
   */
  public String getDir() {
    return dir;
  }

  /**
   * Gets NativeDiscovery.
   *
   * @return native discovery
   */
  public NativeDiscovery getNativeDiscovery() {
    return nativeDiscovery;
  }
}
