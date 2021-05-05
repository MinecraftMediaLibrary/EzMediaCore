package com.github.pulsebeat02.deluxemediaplugin.command.video;

import org.jetbrains.annotations.NotNull;

public enum VideoType {
  ITEMFRAME("itemframe-maps"),
  AREA_EFFECT_CLOUD("area-effect-clouds"),
  CHATBOX("chatbox"),
  DEBUG_HIGHLIGHTS("debug-highlights"),
  SCOREBOARD("scoreboard");

  private final String name;

  VideoType(@NotNull final String name) {
    this.name = name;
  }

  public static VideoType fromString(@NotNull final String str) {
    for (final VideoType type : values()) {
      if (type.getName().equals(str)) {
        return type;
      }
    }
    return null;
  }

  public String getName() {
    return name;
  }
}
