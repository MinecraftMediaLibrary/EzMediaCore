package io.github.pulsebeat02.ezmediacore.image;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
import io.github.pulsebeat02.ezmediacore.dimension.Dimensional;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface MapImage extends LibraryInjectable, Dimensional {

  void draw(final boolean resize) throws IOException;

  void onStartDrawImage();

  @NotNull
  BufferedImage[][] process(@NotNull final BufferedImage image, final boolean resize);

  void onFinishDrawImage();

  void resetMaps();

  @NotNull
  List<Integer> getMaps();

  @NotNull
  Path getImagePath();

  @NotNull
  MapRenderer getRenderer();

  @NotNull
  UUID getIdentifier();
}
