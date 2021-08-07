package io.github.pulsebeat02.ezmediacore.vlc;

import org.jetbrains.annotations.NotNull;

public enum VLCDownloadPortal {

  WIN_64(
      "https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/win64/VLC.zip"),
  MAC_SILICON_64(
      "https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/macos-intel64/VLC.dmg"),
  MAC_AMD_64(
      "https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/macos-arm64/VLC.dmg"),
  WIN_32("https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/win32/VLC.zip"),
  NA("");

  private final String url;

  VLCDownloadPortal(@NotNull final String url) {
    this.url = url;
  }

  public String getUrl() {
    return this.url;
  }

}
