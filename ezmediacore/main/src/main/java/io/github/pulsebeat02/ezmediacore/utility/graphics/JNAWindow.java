package io.github.pulsebeat02.ezmediacore.utility.graphics;

import com.sun.jna.platform.DesktopWindow;
import io.github.pulsebeat02.ezmediacore.player.input.Window;
import java.awt.Rectangle;
import org.jetbrains.annotations.NotNull;

public final class JNAWindow implements Window {

  private final String path;
  private final String title;
  private final int width;
  private final int height;
  private final int x;
  private final int y;

  JNAWindow(@NotNull final DesktopWindow window) {
    final Rectangle rectangle = window.getLocAndSize();
    this.path = window.getFilePath();
    this.title = window.getTitle();
    this.width = (int) rectangle.getWidth();
    this.height = (int) rectangle.getHeight();
    this.x = (int) rectangle.getX();
    this.y = (int) rectangle.getY();
  }

  @Override
  public @NotNull String getFilePath() {
    return this.path;
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  @Override
  public int getX() {
    return this.x;
  }

  @Override
  public int getY() {
    return this.y;
  }

  @Override
  public @NotNull String getTitle() {
    return this.title;
  }
}
