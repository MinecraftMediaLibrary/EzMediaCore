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
package io.github.pulsebeat02.ezmediacore.analysis;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegDownloadPortal;
import io.github.pulsebeat02.ezmediacore.rtp.RTPDownloadPortal;
import io.github.pulsebeat02.ezmediacore.throwable.UnsupportedPlatformException;
import io.github.pulsebeat02.ezmediacore.vlc.VLCDownloadPortal;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class SystemDiagnostics implements Diagnostic {

  private final MediaLibraryCore core;
  private final OperatingSystem system;
  private final CpuArchitecture cpu;
  private final String os;
  private final String ver;

  private FFmpegDownloadPortal ffmpegDownloadLink;
  private VLCDownloadPortal vlcDownloadLink;
  private RTPDownloadPortal rtpDownloadPortal;

  public SystemDiagnostics(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.system = this.getOperatingSystem();
    this.cpu = this.getCpuArchitecture();
    this.os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    this.ver = System.getProperty("os.version").toLowerCase(Locale.ROOT);
    this.initializeDownloadLinks();
    this.debugInformation();
  }

  @Contract(" -> new")
  private @NotNull CpuArchitecture getCpuArchitecture() {
    return new CpuArchitecture(this.getOsArchLower(), this.is64Bit());
  }

  private boolean is64Bit() {
    return this.system.getOSType() == OSType.WINDOWS
        ? this.getProgramFiles()
        : this.getOsArch();
  }

  private String getOsArchLower() {
    return System.getProperty("os.arch").toLowerCase(Locale.ROOT);
  }

  private boolean getProgramFiles() {
    return System.getenv("ProgramFiles(x86)") != null;
  }

  private boolean getOsArch() {
    return System.getProperty("os.arch").contains("64");
  }

  private @NotNull OperatingSystem getOperatingSystem() {
    return new OperatingSystem(this.os, this.getOsType(), this.ver);
  }

  private @NotNull OSType getOsType() {
    return this.isLinux() ? OSType.UNIX : this.isWin() ? OSType.WINDOWS : OSType.MAC;
  }

  private boolean isLinux() {
    return Stream.of("nix", "nux", "aix").anyMatch(this.os::contains);
  }

  private boolean isWin() {
    return this.os.contains("win");
  }

  private static final Map<DeviceInformation, DeviceLinkManager> SYSTEM_TABLE;

  static {
    SYSTEM_TABLE = Map.of(
      DeviceInformation.ofDeviceInfo(true, OSType.WINDOWS, false), DeviceLinkManager.ofDeviceLink(VLCDownloadPortal.WIN_64, FFmpegDownloadPortal.WIN_64, RTPDownloadPortal.WIN_64),
        DeviceInformation.ofDeviceInfo(true, OSType.UNIX, true), DeviceLinkManager.ofDeviceLink(VLCDownloadPortal.NA, FFmpegDownloadPortal.UNIX_ARM_64, RTPDownloadPortal.UNIX_ARM_64),
        DeviceInformation.ofDeviceInfo(true, OSType.UNIX, false), DeviceLinkManager.ofDeviceLink(VLCDownloadPortal.NA, FFmpegDownloadPortal.UNIX_AMD_INTEL_64, RTPDownloadPortal.UNIX_AMD_64),
        DeviceInformation.ofDeviceInfo(true, OSType.MAC, true), DeviceLinkManager.ofDeviceLink(VLCDownloadPortal.MAC_ARM_64, FFmpegDownloadPortal.MAC_64, RTPDownloadPortal.MAC_ARM_64),
        DeviceInformation.ofDeviceInfo(true, OSType.MAC, false), DeviceLinkManager.ofDeviceLink(VLCDownloadPortal.MAC_AMD_64, FFmpegDownloadPortal.MAC_64, RTPDownloadPortal.MAC_AMD_64),
        DeviceInformation.ofDeviceInfo(false, OSType.WINDOWS, false), DeviceLinkManager.ofDeviceLink(VLCDownloadPortal.WIN_32, FFmpegDownloadPortal.WIN_32, RTPDownloadPortal.WIN_32),
        DeviceInformation.ofDeviceInfo(false, OSType.UNIX, true), DeviceLinkManager.ofDeviceLink(VLCDownloadPortal.NA, FFmpegDownloadPortal.UNIX_ARM_32, RTPDownloadPortal.UNIX_ARM_32)
    );
  }


  private void initializeDownloadLinks() {
    final boolean bits64 = this.cpu.isBits64();
    final OSType type = this.system.getOSType();
    final boolean arm = this.getCpuArchitecture().getArchitecture().contains("arm");
    final DeviceLinkManager links = SYSTEM_TABLE.get(new DeviceInformation(bits64, type, arm));
    if (links == null) {
      throw new UnsupportedPlatformException("Unsupported Platform!");
    }
    this.ffmpegDownloadLink = links.getFfmpeg();
    this.rtpDownloadPortal = links.getRtp();
    this.vlcDownloadLink = links.getVlc();
  }

  @Override
  public void debugInformation() {
    final Plugin plugin = this.core.getPlugin();
    final Server server = plugin.getServer();
    Logger.info("===========================================");
    Logger.info("             DEBUG FILE LOGGERS            ");
    Logger.info("===========================================");
    Logger.info("             PLUGIN INFORMATION            ");
    Logger.info("===========================================");
    Logger.info("Plugin Name: %s".formatted(plugin.getName()));
    Logger.info("Plugin Description: %s".formatted(plugin.getDescription()));
    Logger.info("Library Disabled? %s".formatted(this.core.isDisabled()));
    Logger.info("Library Path: %s".formatted(this.core.getLibraryPath()));
    Logger.info("VLC Path: %s".formatted(this.core.getVlcPath()));
    Logger.info("Image Path: %s".formatted(this.core.getImagePath()));
    Logger.info("Audio Path: %s".formatted(this.core.getAudioPath()));
    Logger.info("===========================================");
    Logger.info("             SERVER INFORMATION            ");
    Logger.info("===========================================");
    Logger.info("Server Name: %s".formatted(server.getName()));
    Logger.info("Version: %s".formatted(server.getVersion()));
    Logger.info("Idle Timeout: %d".formatted(server.getIdleTimeout()));
    Logger.info("Online Mode?: %s".formatted(server.getOnlineMode()));
    Logger.info("Player Count: %s".formatted(server.getOnlinePlayers()));
    Logger.info(
        "Plugins: "
            + Arrays.stream(server.getPluginManager().getPlugins())
            .map(Plugin::getName)
            .collect(Collectors.toList()));
    Logger.info("===========================================");
    Logger.info("             SYSTEM INFORMATION            ");
    Logger.info("===========================================");
    Logger.info("Operating System: %s".formatted(this.system.getOSName()));
    Logger.info("Version: %s".formatted(this.system.getVersion()));
    Logger.info("Linux Distribution: %s".formatted(this.system.getLinuxDistribution()));
    Logger.info("CPU Architecture: %s".formatted(this.cpu.getArchitecture()));
    Logger.info("===========================================");
    Logger.info("             INSTALLATION LINKS            ");
    Logger.info("===========================================");
    Logger.info("VLC Installation URL: %s".formatted(this.vlcDownloadLink));
    Logger.info("FFmpeg Installation URL: %s".formatted(this.ffmpegDownloadLink));
    Logger.info("===========================================");
  }

  @Override
  public @NotNull String getFFmpegUrl() {
    return this.ffmpegDownloadLink.getUrl();
  }

  @Override
  public @NotNull String getVlcUrl() {
    return this.vlcDownloadLink.getUrl();
  }

  @Override
  public @NotNull String getRtpUrl() {
    return this.rtpDownloadPortal.getUrl();
  }

  @Override
  public @NotNull OperatingSystemInfo getSystem() {
    return this.system;
  }

  @Override
  public @NotNull CpuInfo getCpu() {
    return this.cpu;
  }
}
