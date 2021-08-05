package io.github.pulsebeat02.ezmediacore.analysis;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
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
  private String vlcDownloadLink;
  private String ffmpegDownloadLink;

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
          this.vlcDownloadLink =
              "https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/win64/VLC.zip";
          this.ffmpegDownloadLink =
              "https://github.com/a-schild/jave2/raw/master/jave-nativebin-win64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-amd64.exe";
        }
        case UNIX -> {
          if (this.cpu.getArchitecture().contains("arm")) {
            Logger.info("Detected Linux ARM 64-bit Operating System");
            this.ffmpegDownloadLink =
                "https://github.com/a-schild/jave2/raw/master/jave-nativebin-arm64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-aarch64";
          } else {
            Logger.info("Detected Linux AMD/Intel 64-bit Operating System");
            this.ffmpegDownloadLink =
                "https://github.com/a-schild/jave2/raw/master/jave-nativebin-linux64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-amd64";
          }
          this.vlcDownloadLink = "N/A";
        }
        case MAC -> {
          if (this.cpu.getArchitecture().contains("amd")) {
            Logger.info("Detected MacOS Silicon 64-bit Operating System");
            this.vlcDownloadLink =
                "https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/macos-intel64/VLC.dmg";
          } else {
            Logger.info("Detected MacOS AMD 64-bit Operating System!");
            this.vlcDownloadLink =
                "https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/macos-arm64/VLC.dmg";
          }
          this.ffmpegDownloadLink =
              "https://github.com/a-schild/jave2/raw/master/jave-nativebin-osx64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-x86_64-osx";
        }
      }
    } else {
      switch (this.system.getOSType()) {
        case WINDOWS -> {
          Logger.info("Detected Windows 32-bit Operating System");
          this.vlcDownloadLink =
              "https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/win32/VLC.zip";
          this.ffmpegDownloadLink =
              "https://github.com/a-schild/jave2/raw/master/jave-nativebin-win32/src/main/resources/ws/schild/jave/nativebin/ffmpeg-x86.exe";
        }
        case UNIX -> {
          if (this.cpu.getArchitecture().contains("arm")) {
            Logger.info("Detected Linux ARM 32-bit Operating System");
            this.vlcDownloadLink = "N/A";
            this.ffmpegDownloadLink =
                "https://github.com/a-schild/jave2/raw/master/jave-nativebin-arm32/src/main/resources/ws/schild/jave/nativebin/ffmpeg-arm";
          }
        }
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
    return this.ffmpegDownloadLink;
  }

  @Override
  public @NotNull String getVlcUrl() {
    return this.vlcDownloadLink;
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
