/**
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.installers;

import java.io.IOException;
import java.nio.file.Path;

public interface Installer {

  /**
   * Downloads the binary into the specified directory with the file name "ffmpeg". If the file
   * already exists there, it will return the path of that file assuming that FFmpeg has already
   * been installed. Otherwise, it downloads a new file. If chmod is set to true, it will change the
   * file permissions to 777 you can use {@ProcessBuilder} or {@Process} to use the binary. It
   * returns the path of the downloaded executable
   *
   * @param chmod whether chmod 777 should be applied (if not windows)
   * @return the path of the download executable
   * @throws IOException if an issue occurred during file creation, downloading, or renaming.
   */
  Path download(final boolean chmod) throws IOException;

  /**
   * Returns whether the operating system is *most likely* supported.
   *
   * @return whether the current operating system is supported or not
   */
  boolean isSupported();
}
