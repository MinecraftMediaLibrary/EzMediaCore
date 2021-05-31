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

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A simple class to handle all the paths the library uses to store configuration files, audio
 * files, video files, and other necessities used throughout the library.
 */
public class LibraryPathHandle {

  private final Path parentFolder;
  private final Path httpParentFolder;
  private final Path dependenciesFolder;
  private final Path vlcFolder;
  private final Path imageFolder;
  private final Path audioFolder;
  private Path ffmpegPath;

  /**
   * Instantiates a new LibraryPathHandle.
   *
   * @param plugin the plugin
   * @param http the http directory
   * @param libraryPath the library path
   * @param vlcPath the vlc path
   * @param imagePath the image path
   * @param audioPath the audio path
   */
  public LibraryPathHandle(
      @NotNull final Plugin plugin,
      @Nullable final String http,
      @Nullable final String libraryPath,
      @Nullable final String vlcPath,
      @Nullable final String imagePath,
      @Nullable final String audioPath) {
    final String path = String.format("%s/mml", plugin.getDataFolder().getAbsolutePath());
    parentFolder = Paths.get(path);
    httpParentFolder = Paths.get(http == null ? String.format("%s/http/", path) : http);
    imageFolder = Paths.get(imagePath == null ? String.format("%s/image/", path) : imagePath);
    audioFolder = Paths.get(audioPath == null ? String.format("%s/audio/", path) : audioPath);
    dependenciesFolder =
        Paths.get(libraryPath == null ? String.format("%s/libraries/", path) : libraryPath);
    vlcFolder = Paths.get(vlcPath == null ? String.format("%s/libraries/vlc/", path) : vlcPath);
    try {
      Files.createDirectories(parentFolder);
      Files.createDirectories(httpParentFolder);
      Files.createDirectories(imageFolder);
      Files.createDirectories(audioFolder);
      Files.createDirectories(dependenciesFolder);
      Files.createDirectories(vlcFolder);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the parent folder of the library.
   *
   * @return the path of the library
   */
  public Path getParentFolder() {
    return parentFolder;
  }

  /**
   * Gets the http parent folder.
   *
   * @return the parent
   */
  public Path getHttpParentFolder() {
    return httpParentFolder;
  }

  /**
   * Gets dependencies folder.
   *
   * @return the dependencies folder
   */
  public Path getDependenciesFolder() {
    return dependenciesFolder;
  }

  /**
   * Gets the vlc folder.
   *
   * @return the vlc folder
   */
  public Path getVlcFolder() {
    return vlcFolder;
  }

  /**
   * Gets the image folder of the library.
   *
   * @return the path of the image folder
   */
  public Path getImageFolder() {
    return imageFolder;
  }

  /**
   * Gets the audio folder of the library.
   *
   * @return the path of the audio folder
   */
  public Path getAudioFolder() {
    return audioFolder;
  }

  /**
   * Get the ffmpeg path to the file.
   *
   * @return the path of the ffmpeg binary
   */
  public Path getFfmpegPath() {
    return ffmpegPath;
  }

  /**
   * Set the ffmpeg path to the file.
   *
   * @param ffmpegPath the path to the ffmpegp binary
   */
  public void setFfmpegPath(final Path ffmpegPath) {
    this.ffmpegPath = ffmpegPath;
  }
}
