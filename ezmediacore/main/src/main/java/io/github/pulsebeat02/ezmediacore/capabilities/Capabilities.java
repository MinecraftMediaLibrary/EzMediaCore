package io.github.pulsebeat02.ezmediacore.capabilities;

public interface Capabilities {
  RTSPCapability RTSP = new RTSPCapability();
  VLCCapability VLC = new VLCCapability();
  FFmpegCapability FFMPEG = new FFmpegCapability();
  YTDLPCapability YT_DLP = new YTDLPCapability();
}
