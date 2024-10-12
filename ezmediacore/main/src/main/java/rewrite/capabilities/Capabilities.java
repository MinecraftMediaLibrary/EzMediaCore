package rewrite.capabilities;

public interface Capabilities {
  RTSPCapability RTSP = new RTSPCapability();
  VLCCapability VLC = new VLCCapability();
  FFmpegCapability FFMPEG = new FFmpegCapability();
}
