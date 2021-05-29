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

import io.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;

/** The base media library class used the media library. */
public interface MediaLibrary {

  /** Shutdown the library instance */
  void shutdown();

  /**
   * Gets plugin.
   *
   * @return the plugin
   */
  Plugin getPlugin();

  /**
   * Gets handler.
   *
   * @return the handler
   */
  PacketHandler getHandler();

  /**
   * Whether the library is using vlcj.
   *
   * @return the boolean
   */
  boolean isVlcj();

  /**
   * Sets the usage status of vlcj.
   *
   * @param vlcj status
   */
  void setVlcj(boolean vlcj);

  /**
   * Gets the path of the parent library folder.
   *
   * @return the path
   */
  Path getPath();

  /**
   * Gets the http parent folder.
   *
   * @return the parent
   */
  Path getHttpParentFolder();

  /**
   * Gets dependencies folder.
   *
   * @return the dependencies folder
   */
  Path getDependenciesFolder();

  /**
   * Gets the vlc folder.
   *
   * @return the vlc folder
   */
  Path getVlcFolder();

  /**
   * Gets the parent folder of the library.
   *
   * @return the path of the library
   */
  Path getParentFolder();

  /**
   * Gets the image folder of the library.
   *
   * @return the path of the image folder
   */
  Path getImageFolder();

  /**
   * Gets the audio folder of the library.
   *
   * @return the path of the audio folder
   */
  Path getAudioFolder();

  /**
   * Gets the library path handle.
   *
   * @return the path handle
   */
  LibraryPathHandle getHandle();

  /**
   * Returns the status of the library.
   *
   * @return whether the library is disabled or not
   */
  boolean isDisabled();

  /**
   * Gets the listener.
   *
   * @return the listener
   */
  Listener getRegistrationHandler();
}
