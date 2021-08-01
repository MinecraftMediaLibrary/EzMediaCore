package io.github.pulsebeat02.epicmedialib.resourcepack;

public enum PackFormat {
  VER_115(5),
  VER_1151(5),
  VER_1152(5),
  VER_1161(6),
  VER_1162(6),
  VER_1163(6),
  VER_1164(6),
  VER_1165(6),
  VER_117(7),
  VER_1171(7),
  VER_UNKNOWN(-1);

  private final int id;

  PackFormat(final int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
