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

package com.github.pulsebeat02.minecraftmedialibrary.dependency;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * A special handling class specific to the JAVE2 library. JAVE2 is an extension to * "JAVE", an
 * audio transcribing framework which is useful for converting to OGG Vorbis files * and adjusting
 * audio quality to necessary settings. Because JAVE2 uses modules which are * operating system
 * dependent, this special class will handle the correct installation based on * the environment of
 * the library. We use these separate modules instead of using one whole combined one to save some
 * space for the user. We then end up overriding the get path method to point it to the correct
 * ffmpeg location.
 *
 * @see <a href="https://github.com/a-schild/jave2">JAVE2 Github</a>.
 */
public class FFmpegDependencyInstallation {

  private static String FFMPEG_PATH;

  private final String dependencyFolder;
  private File file;

  /**
   * Instantiates a new JaveDependencyInstallation
   *
   * @param library library
   */
  public FFmpegDependencyInstallation(@NotNull final MinecraftMediaLibrary library) {
    this(library.getDependenciesFolder());
  }

  /**
   * Instantiates a new JaveDependencyInstallation
   *
   * @param dependency directory path
   */
  public FFmpegDependencyInstallation(@NotNull final String dependency) {
    dependencyFolder = dependency + "/ffmpeg/";
    final File folder = new File(dependencyFolder);
    if (folder.mkdir()) {
      Logger.info("Created FFMPEG Folder");
    }
  }

  /**
   * Gets the path of the executable ffmpeg binary.
   *
   * @return the path of the ffmpeg library
   */
  public static String getFFmpegPath() {
    return FFMPEG_PATH;
  }

  /** Installs FFMPEG Resource */
  public void install() {
    try {
      file = downloadFFmpeg();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    FFMPEG_PATH = file.getAbsolutePath();
  }

  /**
   * Downloads the proper FFMPEG binary file.
   *
   * @return the FFMPEG file
   * @throws IOException if an issue occurred during downloading
   */
  public File downloadFFmpeg() throws IOException {
    final File folder = new File(dependencyFolder);
    mkdir(folder);
    File file = searchFFMPEG(folder);
    if (file != null) {
      return file;
    }
    final String fileUrl = getFFmpegUrl();
    final URL url = new URL(fileUrl);
    file = new File(dependencyFolder, FilenameUtils.getName(url.getPath()));
    FileUtils.copyURLToFile(url, file);
    return file;
  }

  /**
   * Searches for existing FFMPEG dependency file.
   *
   * @param folder the folder file
   * @return file
   */
  public File searchFFMPEG(@NotNull final File folder) {
    for (final File f : folder.listFiles()) {
      if (f.getName().contains("ffmpeg")) {
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

  /**
   * Gets artifact id of FFMPEG dependency.
   *
   * @return the artifact id
   */
  private String getFFmpegUrl() {
    Logger.info("Detecting Operating System...");
    final String arch = System.getProperty("os.name").toLowerCase();
    String artifactId = "INVALID_OPERATING_SYSTEM";
    if (RuntimeUtilities.is64bit()) {
      if (RuntimeUtilities.isWindows()) {
        Logger.info("Detected Windows 64 Bit!");
        artifactId =
            "https://github.com/a-schild/jave2/raw/master/jave-nativebin-win64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-amd64.exe";
      } else if (RuntimeUtilities.isLinux()) {
        if (arch.contains("arm")) {
          Logger.info("Detected Linux ARM 64 Bit!");
          artifactId =
              "https://github.com/a-schild/jave2/raw/master/jave-nativebin-arm64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-aarch64";
        } else {
          Logger.info("Detected Linux AMD/Intel 64 Bit!");
          artifactId =
              "https://github.com/a-schild/jave2/raw/master/jave-nativebin-linux64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-amd64";
        }
      } else if (RuntimeUtilities.isMac()) {
        Logger.info("Detected MACOS 64 Bit!");
        artifactId =
            "https://github.com/a-schild/jave2/raw/master/jave-nativebin-osx64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-x86_64-osx";
      }
    } else {
      if (RuntimeUtilities.isLinux()) {
        if (arch.contains("arm")) {
          Logger.info("Detected ARM 32 Bit!");
          artifactId =
              "https://github.com/a-schild/jave2/raw/master/jave-nativebin-arm32/src/main/resources/ws/schild/jave/nativebin/ffmpeg-arm";
        }
      }
    }
    return artifactId;
  }
}
