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
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.VLCBinarySearcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.binding.RuntimeUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractSilentOSDependentSolution implements SilentOSDependentSolution {

  private final Path dir;

  /**
   * Instantiates a new SilentOSDependentSolution.
   *
   * @param library the library
   */
  protected AbstractSilentOSDependentSolution(@NotNull final MediaLibrary library) {
    this(library.getVlcFolder());
  }

  /**
   * Instantiates a new SilentOSDependentSolution.
   *
   * @param dir the directory
   */
  protected AbstractSilentOSDependentSolution(@NotNull final Path dir) {
    this.dir = dir;
  }

  /**
   * Loads native dependency from file.
   *
   * @param folder directory
   */
  @Override
  public void loadNativeDependency(@NotNull final Path folder) {
    NativeLibrary.addSearchPath(
        RuntimeUtil.getLibVlcLibraryName(), folder.toAbsolutePath().toString());
    new VLCBinarySearcher(folder).search();
  }

  /** Prints all System environment variables. */
  @Override
  public void printSystemEnvironmentVariables() {
    Logger.info("======== SYSTEM ENVIRONMENT VARIABLES ========");
    for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
      Logger.info(String.format("Key: %s | Entry: %s", entry.getKey(), entry.getValue()));
    }
    Logger.info("==============================================");
  }

  /** Prints all System properties. */
  @Override
  public void printSystemProperties() {
    Logger.info("============== SYSTEM PROPERTIES ==============");
    final Properties p = System.getProperties();
    final Enumeration<Object> keys = p.keys();
    while (keys.hasMoreElements()) {
      final String key = (String) keys.nextElement();
      Logger.info(String.format("Key: %s | Entry: %s", key, p.get(key)));
    }
    Logger.info("===============================================");
  }

  /**
   * Gets VLC folder in folder.
   *
   * @param folder search folder
   * @return file
   */
  @Override
  @Nullable
  public Path findVLCFolder(@NotNull final Path folder) {
    try (final Stream<Path> paths = Files.walk(folder)) {
      final List<Path> p = paths.collect(Collectors.toList());
      for (final Path f : p) {
        final String name = PathUtilities.getName(f);
        if (StringUtils.containsIgnoreCase(name, "vlc") && !name.endsWith(".dmg")) {
          return f;
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Deletes file (archive).
   *
   * @param zip archive
   */
  @Override
  public void deleteArchive(@NotNull final Path zip) {
    Logger.info("Deleting Archive...");
    try {
      Files.delete(zip);
      Logger.info("Archive deleted after installation.");
    } catch (final IOException e) {
      Logger.error("Archive could NOT be deleted after installation!");
      e.printStackTrace();
    }
  }

  @Override
  public Path getDir() {
    return dir;
  }
}
