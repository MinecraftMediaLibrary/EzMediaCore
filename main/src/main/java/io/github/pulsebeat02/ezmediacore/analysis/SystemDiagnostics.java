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
import io.github.pulsebeat02.ezmediacore.throwable.UnsupportedPlatformException;
import io.github.pulsebeat02.ezmediacore.vlc.VLCDownloadPortal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class SystemDiagnostics implements Diagnostic {

  private final MediaLibraryCore core;
  private final OperatingSystem system;
  private final CpuArchitecture cpu;
  private final List<Mixer> sound;
  private FFmpegDownloadPortal ffmpegDownloadLink;
  private VLCDownloadPortal vlcDownloadLink;


  public SystemDiagnostics(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.system = this.getOperatingSystem();
    this.cpu = this.getCpuArchitecture();
    this.sound = this.getMixers();
    this.initializeDownloadLinks();
    this.debugInformation();
  }

  private CpuArchitecture getCpuArchitecture() {
    return new CpuArchitecture(
        System.getProperty("os.arch").toLowerCase(Locale.ROOT),
        this.system.getOSType() == OSType.WINDOWS
            ? System.getenv("ProgramFiles(x86)") != null
            : System.getProperty("os.arch").contains("64"));
  }

  private OperatingSystem getOperatingSystem() {
    final String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    return new OperatingSystem(
        os,
        os.contains("nix") || os.contains("nux") || os.contains("aix")
            ? OSType.UNIX
            : os.contains("win") ? OSType.WINDOWS : OSType.MAC,
        System.getProperty("os.version"));
  }

  private List<Mixer> getMixers() {
    final List<Mixer> mixers = new ArrayList<>();
    final Mixer.Info[] devices = AudioSystem.getMixerInfo();
    final Line.Info sourceInfo = new Line.Info(SourceDataLine.class);
    for (final Mixer.Info mixerInfo : devices) {
      final Mixer mixer = AudioSystem.getMixer(mixerInfo);
      if (mixer.isLineSupported(sourceInfo)) {
        mixers.add(mixer);
      }
    }
    return mixers;
  }

  private void initializeDownloadLinks() {
    if (this.cpu.isBits64()) {
      switch (this.system.getOSType()) {
        case WINDOWS -> {
          Logger.info("Detected Windows 64-bit Operating System");
          this.vlcDownloadLink = VLCDownloadPortal.WIN_64;
          this.ffmpegDownloadLink = FFmpegDownloadPortal.WIN_64;
        }
        case UNIX -> {
          if (this.cpu.getArchitecture().contains("arm")) {
            Logger.info("Detected Linux ARM 64-bit Operating System");
            this.ffmpegDownloadLink = FFmpegDownloadPortal.UNIX_ARM_64;
          } else {
            Logger.info("Detected Linux AMD/Intel 64-bit Operating System");
            this.ffmpegDownloadLink = FFmpegDownloadPortal.UNIX_AMD_INTEL_64;
          }
          this.vlcDownloadLink = VLCDownloadPortal.NA;
        }
        case MAC -> {
          if (this.cpu.getArchitecture().contains("amd")) {
            Logger.info("Detected MacOS Silicon 64-bit Operating System");
            this.vlcDownloadLink = VLCDownloadPortal.MAC_SILICON_64;
          } else {
            Logger.info("Detected MacOS AMD 64-bit Operating System!");
            this.vlcDownloadLink = VLCDownloadPortal.MAC_AMD_64;
          }
          this.ffmpegDownloadLink = FFmpegDownloadPortal.MAC_64;
        }
        default -> throw new UnsupportedPlatformException("UNKNOWN");
      }
    } else {
      switch (this.system.getOSType()) {
        case WINDOWS -> {
          Logger.info("Detected Windows 32-bit Operating System");
          this.vlcDownloadLink = VLCDownloadPortal.WIN_32;
          this.ffmpegDownloadLink = FFmpegDownloadPortal.WIN_32;
        }
        case UNIX -> {
          if (this.cpu.getArchitecture().contains("arm")) {
            Logger.info("Detected Linux ARM 32-bit Operating System");
            this.vlcDownloadLink = VLCDownloadPortal.NA;
            this.ffmpegDownloadLink = FFmpegDownloadPortal.UNIX_ARM_32;
          }
        }
        default -> throw new UnsupportedPlatformException("UNKNOWN");
      }
    }
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
  public @NotNull OperatingSystemInfo getSystem() {
    return this.system;
  }

  @Override
  public @NotNull CpuInfo getCpu() {
    return this.cpu;
  }

  @Override
  public @NotNull List<Mixer> getSound() {
    return this.sound;
  }
}
