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

package com.github.pulsebeat02.minecraftmedialibrary.dependency;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.DependencyUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * A special handling class specific to the JAVE2 library. JAVE2 is an extension to * "JAVE", an
 * audio transcribing framework which is useful for converting to OGG Vorbis files * and adjusting
 * audio quality to necessary settings. Because JAVE2 uses modules which are * operating system
 * dependent, this special class will handle the correct installation based on * the environemnt of
 * the library.
 *
 * @see <a href="https://github.com/a-schild/jave2">JAVE2 Github</a>.
 */
public class JaveDependencyInstallation {

  private final String dependencyFolder;
  private File file;

  /**
   * Instantiates a new JaveDependencyInstallation
   *
   * @param library library
   */
  public JaveDependencyInstallation(@NotNull final MinecraftMediaLibrary library) {
    dependencyFolder = library.getDependenciesFolder();
  }

  /**
   * Instantiates a new JaveDependencyInstallation
   *
   * @param dependency directory path
   */
  public JaveDependencyInstallation(@NotNull final String dependency) {
    dependencyFolder = dependency;
  }

  /**
   * Install and Loads Jave Dependency
   *
   * @return Jave binary file
   */
  public File install() {
    final File folder = new File(dependencyFolder);
    mkdir(folder);
    File file = searchJaveDependency(folder);
    if (file != null) {
      return file;
    }
    try {
      file =
          DependencyUtilities.downloadFile(
              "ws.schild",
              getArtifactId(),
              "2.7.3",
              dependencyFolder,
              DependencyResolution.MAVEN_DEPENDENCY);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return file;
  }

  /**
   * Searches for existing Jave dependency file.
   *
   * @param folder the folder file
   * @return file
   */
  public File searchJaveDependency(@NotNull final File folder) {
    for (final File f : folder.listFiles()) {
      if (f.getName().contains("jave")) {
        file = f;
        return file;
      }
    }
    return null;
  }

  /**
   * Creates directory if not existent.
   *
   * @param folder the folder file
   */
  public void mkdir(@NotNull final File folder) {
    if (!folder.exists()) {
      if (folder.mkdir()) {
        Logger.info("Library folder created successfully");
      } else {
        Logger.error("Library folder couldn't created successfully");
      }
    }
  }

  /** Load. */
  public void load() {
    try {
      if (file != null) {
        DependencyUtilities.loadDependency(file);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets artifact id of Jave dependency.
   *
   * @return the artifact id
   */
  private String getArtifactId() {
    Logger.info("Detecting Operating System...");
    final String os = System.getProperty("os.name").toLowerCase();
    String artifactId = "INVALID_OPERATING_SYSTEM";
    final boolean linux = os.contains("nix") || os.contains("nux") || os.contains("aix");
    if (RuntimeUtilities.is64Architecture(os)) {
      if (os.contains("win")) {
        Logger.info("Detected Windows 64 Bit!");
        artifactId = "jave-nativebin-win64";
      } else if (linux) {
        if (os.contains("arm")) {
          Logger.info("Detected Linux ARM 64 Bit!");
          artifactId = "jave-nativebin-linux-arm64";
        } else {
          Logger.info("Detected Linux AMD/Intel 64 Bit!");
          artifactId = "jave-nativebin-linux64";
        }
      } else if (os.contains("mac")) {
        Logger.info("Detected MACOS 64 Bit!");
        artifactId = "jave-nativebin-osx64";
      }
    } else {
      if (linux) {
        if (os.contains("arm")) {
          Logger.info("Detected ARM 32 Bit!");
          artifactId = "jave-nativebin-linux-arm32";
        }
      }
    }
    return artifactId;
  }
}
