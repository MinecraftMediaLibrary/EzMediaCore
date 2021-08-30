/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.vlc;

import org.jetbrains.annotations.NotNull;

public enum VLCDownloadPortal {

  WIN_64(
      "https://get.videolan.org/vlc/%VLC_VERSION%/win64/vlc-%VLC_VERSION%-win64.zip"), // https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/win64/VLC.zip
  MAC_SILICON_64(
      "https://get.videolan.org/vlc/%VLC_VERSION%/macosx/vlc-%VLC_VERSION%-intel64.dmg"), // https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/macos-intel64/VLC.dmg
  MAC_AMD_64(
      "https://get.videolan.org/vlc/%VLC_VERSION%/macosx/vlc-%VLC_VERSION%-arm64.dmg"), // https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/macos-arm64/VLC.dmg
  WIN_32(
      "https://get.videolan.org/vlc/%VLC_VERSION%/win32/vlc-%VLC_VERSION%-win32.zip"), // https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/win32/VLC.zip
  NA("");

  public static final String VLC_VERSION;

  static {
    VLC_VERSION = "3.0.16";
  }

  private final String url;

  VLCDownloadPortal(@NotNull final String url) {
    this.url = url.replaceAll("%VLC_VERSION%", "3.0.16");
  }

  public String getUrl() {
    return this.url;
  }

}
