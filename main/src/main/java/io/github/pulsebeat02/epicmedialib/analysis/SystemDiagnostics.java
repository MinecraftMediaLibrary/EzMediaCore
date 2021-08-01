package io.github.pulsebeat02.epicmedialib.analysis;

import io.github.pulsebeat02.epicmedialib.Logger;
import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
  private final Collection<Mixer> sound;
  private String vlcDownloadLink;
  private String ffmpegDownloadLink;

  public SystemDiagnostics(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.cpu = getCpuArchitecture();
    this.system = getOperatingSystem();
    this.sound = getMixers();
    initializeDownloadLinks();
    debugInformation();
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

  private Collection<Mixer> getMixers() {
    final Collection<Mixer> mixers = new ArrayList<>();
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
        case WINDOWS:
          Logger.info("Detected Windows 64-bit Operating System");
          this.vlcDownloadLink =
              "https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/win64/VLC.zip";
          this.ffmpegDownloadLink =
              "https://github.com/a-schild/jave2/raw/master/jave-nativebin-win64/src/main/resources/ws/schild/jave/nativebin/ffmpeg-amd64.exe";
          break;
        case UNIX:
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
          break;
        case MAC:
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
          break;
      }
    } else {
      switch (this.system.getOSType()) {
        case WINDOWS:
          Logger.info("Detected Windows 32-bit Operating System");
          this.vlcDownloadLink =
              "https://github.com/MinecraftMediaLibrary/VLC-Release-Mirror/raw/master/win32/VLC.zip";
          this.ffmpegDownloadLink =
              "https://github.com/a-schild/jave2/raw/master/jave-nativebin-win32/src/main/resources/ws/schild/jave/nativebin/ffmpeg-x86.exe";
          break;
        case UNIX:
          if (this.cpu.getArchitecture().contains("arm")) {
            Logger.info("Detected Linux ARM 32-bit Operating System");
            this.vlcDownloadLink = "N/A";
            this.ffmpegDownloadLink =
                "https://github.com/a-schild/jave2/raw/master/jave-nativebin-arm32/src/main/resources/ws/schild/jave/nativebin/ffmpeg-arm";
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
    Logger.info(String.format("Plugin Name: %s", plugin.getName()));
    Logger.info(String.format("Plugin Description: %s", plugin.getDescription()));
    Logger.info(String.format("HTTP Server Path: %s", this.core.getHttpServerPath()));
    Logger.info(String.format("Library Disabled? %s", this.core.isDisabled()));
    Logger.info(String.format("Library Path: %s", this.core.getLibraryPath()));
    Logger.info(String.format("VLC Path: %s", this.core.getVlcPath()));
    Logger.info(String.format("Image Path: %s", this.core.getImagePath()));
    Logger.info(String.format("Audio Path: %s", this.core.getAudioPath()));
    Logger.info("===========================================");
    Logger.info("             SERVER INFORMATION            ");
    Logger.info("===========================================");
    Logger.info(String.format("Server Name: %s", server.getName()));
    Logger.info(String.format("Version: %s", server.getVersion()));
    Logger.info(String.format("Idle Timeout: %d", server.getIdleTimeout()));
    Logger.info(String.format("Online Mode?: %s", server.getOnlineMode()));
    Logger.info(String.format("Player Count: %s", server.getOnlinePlayers()));
    Logger.info(
        "Plugins: "
            + Arrays.stream(server.getPluginManager().getPlugins())
                .map(Plugin::getName)
                .collect(Collectors.toList()));
    Logger.info("===========================================");
    Logger.info("             SYSTEM INFORMATION            ");
    Logger.info("===========================================");
    Logger.info(String.format("Operating System: %s", this.system.getOSName()));
    Logger.info(String.format("Version: %s", this.system.getVersion()));
    Logger.info(String.format("Linux Distribution: %s", this.system.getLinuxDistribution()));
    Logger.info(String.format("CPU Architecture: %s", this.cpu.getArchitecture()));
    Logger.info("===========================================");
    Logger.info("             INSTALLATION LINKS            ");
    Logger.info("===========================================");
    Logger.info(String.format("VLC Installation URL: %s", this.vlcDownloadLink));
    Logger.info(String.format("FFmpeg Installation URL: %s", this.ffmpegDownloadLink));
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
  public @NotNull Collection<Mixer> getSound() {
    return this.sound;
  }
}
