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
package io.github.pulsebeat02.ezmediacore.rtp;

import org.jetbrains.annotations.NotNull;

public enum RTPDownloadPortal {
  WIN_64(
      "https://github.com/aler9/rtsp-simple-server/releases/download/v0.17.3/rtsp-simple-server_v0.17.3_windows_amd64.zip"),
  WIN_32(
      "https://github.com/MinecraftMediaLibrary/rtsmp-binaries/raw/main/rtsp-simple-server_v0.17.3_windows_i386.zip"),

  UNIX_AMD_64(
      "https://github.com/aler9/rtsp-simple-server/releases/download/v0.17.3/rtsp-simple-server_v0.17.3_linux_amd64.tar.gz"),
  UNIX_ARM_64(
      "https://github.com/MinecraftMediaLibrary/rtsmp-binaries/raw/main/rtsp-simple-server_v0.17.3_linux_arm64.zip"),
  UNIX_ARM_32(
      "https://github.com/MinecraftMediaLibrary/rtsmp-binaries/raw/main/rtsp-simple-server_v0.17.3_linux_arm.zip"),

  MAC_AMD_64(
      "https://github.com/aler9/rtsp-simple-server/releases/download/v0.17.3/rtsp-simple-server_v0.17.3_darwin_amd64.tar.gz"),
  MAC_ARM_64(
      "https://github.com/MinecraftMediaLibrary/rtsmp-binaries/raw/main/rtsp-simple-server_v0.17.3_darwin_arm64.zip");

  private final String url;

  RTPDownloadPortal(@NotNull final String url) {
    this.url = url;
  }

  public String getUrl() {
    return this.url;
  }
}
