package io.github.pulsebeat02.ezmediacore.capabilities;

public enum SelectCapability {
  VLC, RTSP, FFMPEG, YT_DLP;

  public static final SelectCapability[] DEFAULT_CAPABILITIES = {VLC, RTSP, FFMPEG, YT_DLP};
}
