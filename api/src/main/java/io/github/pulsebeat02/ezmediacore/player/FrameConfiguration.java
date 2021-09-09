package io.github.pulsebeat02.ezmediacore.player;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class FrameConfiguration {

  public static FrameConfiguration FPS_10;
  public static FrameConfiguration FPS_15;
  public static FrameConfiguration FPS_20;
  public static FrameConfiguration FPS_25;
  public static FrameConfiguration FPS_30;
  public static FrameConfiguration FPS_35;

  static {
    FPS_10 = ofFps(10);
    FPS_15 = ofFps(15);
    FPS_20 = ofFps(20);
    FPS_25 = ofFps(25);
    FPS_30 = ofFps(30);
    FPS_35 = ofFps(35);
  }

  private final int fps;

  FrameConfiguration(final int fps) {
    this.fps = fps;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull FrameConfiguration ofFps(final int fps) {
    return new FrameConfiguration(fps);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull FrameConfiguration ofFps(@NotNull final Number fps) {
    return ofFps((int) fps);
  }

  public int getFps() {
    return this.fps;
  }
}
