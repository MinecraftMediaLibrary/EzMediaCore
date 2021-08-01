package io.github.pulsebeat02.epicmedialib.playlist.youtube;

public enum VideoQuality {
  UNKNOWN(0),
  NO_VIDEO(0),
  TINY(1),
  SMALL(2), // 240p
  MEDIUM(3), // 360p
  LARGE(4), // 480p
  HD720(5),
  HD1080(6),
  HD1440(7),
  HD2160(8),
  HD2880P(9),
  HIGH_RES(10); // 3072p

  private final int order;

  VideoQuality(final int order) {
    this.order = order;
  }

  public int getOrder() {
    return this.order;
  }
}
