package io.github.pulsebeat02.epicmedialib.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public final class OperatingSystem implements OperatingSystemInfo {

  private final String osName;
  private final OSType type;
  private final String version;
  private final String linuxDistro;

  public OperatingSystem(
      @NotNull final String osName, @NotNull final OSType type, @NotNull final String version) {
    this.osName = osName;
    this.type = type;
    this.version = version;
    this.linuxDistro = type == OSType.UNIX ? retrieveLinuxDistribution() : "";
  }

  private String retrieveLinuxDistribution() {
    final String[] cmd = {"/bin/sh", "-c", "cat /etc/*-release"};
    final StringBuilder concat = new StringBuilder();
    try {
      final Process p = Runtime.getRuntime().exec(cmd);
      final BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      while ((line = bri.readLine()) != null) {
        concat.append(line);
        concat.append(" ");
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return concat.toString();
  }

  @Override
  public @NotNull String getOSName() {
    return this.osName;
  }

  @Override
  public @NotNull OSType getOSType() {
    return this.type;
  }

  @Override
  public @NotNull String getLinuxDistribution() {
    return this.linuxDistro;
  }

  @Override
  public @NotNull String getVersion() {
    return this.version;
  }

  @Override
  public String toString() {
    return String.format(
        "{os=%s,type=%s,linux-distro=%s}",
        this.osName, this.type.name().toLowerCase(Locale.ROOT), this.linuxDistro);
  }
}
