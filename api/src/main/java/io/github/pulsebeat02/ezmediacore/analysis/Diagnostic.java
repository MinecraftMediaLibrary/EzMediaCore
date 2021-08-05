package io.github.pulsebeat02.ezmediacore.analysis;

import java.util.List;
import javax.sound.sampled.Mixer;
import org.jetbrains.annotations.NotNull;

/**
 * This class primarily contains all Diagnostic information including the proper Operating System
 * Information used to download the correct dependencies, sound drivers installed, CPU information,
 * and URLs that point to the correct download locations for the current server hardware.
 */
public interface Diagnostic {

  /**
   * Debugs the information into the Logger class the library uses to debug information for
   * clients.
   */
  void debugInformation();

  /**
   * Gets the proper FFmpeg installation link based on the current server hardware.
   *
   * @return the FFmpeg installation url
   */
  @NotNull
  String getFFmpegUrl();

  /**
   * Gets the proper VLC installation link based on the current server hardware.
   *
   * @return the VLC installation url
   */
  @NotNull
  String getVlcUrl();

  /**
   * Gets the information surrounding the Operating System.
   *
   * @return the operating system information
   */
  @NotNull
  OperatingSystemInfo getSystem();

  /**
   * Gets the information surrounding the CPU.
   *
   * @return the CPU information
   */
  @NotNull
  CpuInfo getCpu();

  /**
   * Gets all sound drivers the system is associated with.
   *
   * @return a List of all sound drivers connected
   */
  @NotNull
  List<Mixer> getSound();
}
