package io.github.pulsebeat02.ezmediacore.player.output.vlc;

import io.github.pulsebeat02.ezmediacore.player.output.OutputConfiguration;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class VLCStandardOutput extends OutputConfiguration {

  public static final String ACCESS;
  public static final String MUX;
  public static final String DST;
  public static final String BIND;
  public static final String PATH;
  public static final String SAP;
  public static final String NAME;
  public static final String DESCRIPTION;
  public static final String URL;
  public static final String EMAIL;

  static {
    ACCESS = "access";
    MUX = "mux";
    DST = "dst";
    BIND = "bind";
    PATH = "path";
    SAP = "sap";
    NAME = "name";
    DESCRIPTION = "description";
    URL = "url";
    EMAIL = "email";
  }

  @Override
  public @NotNull String toString() {
    final Map<String, String> configuration = this.getConfiguration();
    final StringBuilder builder = new StringBuilder("std{");
    for (final Map.Entry<String, String> entry : configuration.entrySet()) {
      builder.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
    }
    builder.append("}");
    return builder.toString();
  }
}
