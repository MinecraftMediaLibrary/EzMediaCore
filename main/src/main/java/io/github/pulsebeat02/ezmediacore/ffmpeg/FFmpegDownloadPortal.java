package io.github.pulsebeat02.ezmediacore.ffmpeg;

import org.jetbrains.annotations.NotNull;

public enum FFmpegDownloadPortal {

  WIN_64(
      "https://github.com/a-schild/jave2/raw/master/jave-nativebin-win64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-amd64.exe"),
  UNIX_ARM_64(
      "https://github.com/a-schild/jave2/raw/master/jave-nativebin-arm64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-aarch64"),
  UNIX_AMD_INTEL_64(
      "https://github.com/a-schild/jave2/raw/master/jave-nativebin-linux64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-amd64"),
  MAC_64(
      "https://github.com/a-schild/jave2/raw/master/jave-nativebin-osx64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-x86_64-osx"),
  WIN_32(
      "https://github.com/a-schild/jave2/raw/master/jave-nativebin-win32/src/main/resources/ws/schild/jave/nativebin/ffmpeg-x86.exe"),
  UNIX_ARM_32(
      "https://github.com/a-schild/jave2/raw/master/jave-nativebin-arm32/src/main/resources/ws/schild/jave/nativebin/ffmpeg-arm");

  private final String url;

  FFmpegDownloadPortal(@NotNull final String url) {
    this.url = url;
  }

  public String getUrl() {
    return this.url;
  }
}
