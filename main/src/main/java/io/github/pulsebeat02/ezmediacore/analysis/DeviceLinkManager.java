package io.github.pulsebeat02.ezmediacore.analysis;

import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegDownloadPortal;
import io.github.pulsebeat02.ezmediacore.rtp.RTPDownloadPortal;
import io.github.pulsebeat02.ezmediacore.vlc.VLCDownloadPortal;
import org.jetbrains.annotations.NotNull;

public final class DeviceLinkManager {

  private final VLCDownloadPortal vlc;
  private final FFmpegDownloadPortal ffmpeg;
  private final RTPDownloadPortal rtp;

  DeviceLinkManager(
      @NotNull final VLCDownloadPortal vlc,
      @NotNull final FFmpegDownloadPortal ffmpeg,
      @NotNull final RTPDownloadPortal rtp) {
    this.vlc = vlc;
    this.ffmpeg = ffmpeg;
    this.rtp = rtp;
  }

  public static @NotNull DeviceLinkManager ofDeviceLink(
      @NotNull final VLCDownloadPortal vlc,
      @NotNull final FFmpegDownloadPortal ffmpeg,
      @NotNull final RTPDownloadPortal rtp) {
    return new DeviceLinkManager(vlc, ffmpeg, rtp);
  }

  public @NotNull VLCDownloadPortal getVlc() {
    return this.vlc;
  }

  public @NotNull FFmpegDownloadPortal getFfmpeg() {
    return this.ffmpeg;
  }

  public @NotNull RTPDownloadPortal getRtp() {
    return this.rtp;
  }
}
