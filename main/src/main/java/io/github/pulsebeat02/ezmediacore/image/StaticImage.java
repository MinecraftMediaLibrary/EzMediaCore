package io.github.pulsebeat02.ezmediacore.image;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.utility.ImmutableDimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.jetbrains.annotations.NotNull;

public class StaticImage extends Image implements MapImage {

  private final BufferedImage image;

  public StaticImage(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path image,
      final int[][] maps,
      @NotNull final ImmutableDimension dimension)
      throws IOException {
    super(core, image, maps, dimension);
    this.image = ImageIO.read(image.toFile());
  }

  @Override
  public void draw(final boolean resize) {
    onStartDrawImage();
    getRenderer().drawMap(process(image, resize));
    onFinishDrawImage();
  }
}
