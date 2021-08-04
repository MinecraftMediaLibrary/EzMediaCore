package io.github.pulsebeat02.ezmediacore.playlist.youtube;

public enum AudioQuality {
  UNKNOWN(0),
  NOAUDIO(0),
  LOW(1),
  MEDIUM(2),
  HIGH(3);

  private final int order;

  AudioQuality(final int order) {
    this.order = order;
  }

  public Integer getOrder() {
    return this.order;
  }
}
