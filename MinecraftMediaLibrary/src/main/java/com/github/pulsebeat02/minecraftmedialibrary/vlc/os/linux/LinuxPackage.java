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

package com.github.pulsebeat02.minecraftmedialibrary.vlc.os.linux;

import org.jetbrains.annotations.NotNull;

/**
 * This instantiates a new LinuxPackage which is used for easy installation. It will specify the
 * correct CPU Architecture the package is for, the url asociated with such a package, and the
 * mirror Github url if the main mirror is down.
 */
public class LinuxPackage {

  private final CPUArchitecture arch;
  private final String url;
  private final String mirror;

  /**
   * Instantiates a new LinuxPackage.
   *
   * @param url the url
   * @param arch the arch
   */
  public LinuxPackage(@NotNull final String url, @NotNull final CPUArchitecture arch) {
    this.arch = arch;
    this.url = url;
    mirror =
        "https://github.com/PulseBeat02/VLC-Release-Mirror/raw/master/linux/"
            + url.substring(url.lastIndexOf("/") + 1);
  }

  /**
   * Instantiates a new LinuxPackage.
   *
   * @param url the url
   * @param mirror the mirror url
   * @param arch the arch
   */
  public LinuxPackage(
      @NotNull final String url,
      @NotNull final String mirror,
      @NotNull final CPUArchitecture arch) {
    this.arch = arch;
    this.url = url;
    this.mirror = mirror;
  }

  /**
   * Gets CPU Architecture.
   *
   * @return CPU Architecture of package
   */
  public CPUArchitecture getArch() {
    return arch;
  }

  /**
   * Gets the package URL.
   *
   * @return the package URL
   */
  public String getUrl() {
    return url;
  }

  /**
   * Gets the mirror URL.
   *
   * @return the mirror URL
   */
  public String getMirror() {
    return mirror;
  }
}
