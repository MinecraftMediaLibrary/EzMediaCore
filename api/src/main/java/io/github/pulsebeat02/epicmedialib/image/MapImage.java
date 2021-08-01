package io.github.pulsebeat02.epicmedialib.image;

import io.github.pulsebeat02.epicmedialib.LibraryInjectable;
import io.github.pulsebeat02.epicmedialib.utility.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface MapImage extends LibraryInjectable, Dimension {

  void draw(final boolean resize) throws IOException;

  void onStartDrawImage();

  @NotNull
  BufferedImage[][] process(@NotNull final BufferedImage image, final boolean resize);

  void onFinishDrawImage();

  int[][] getMapMatrix();

  @NotNull
  Path getImagePath();

  @NotNull
  MapRenderer getRenderer();
}
