package io.github.pulsebeat02.ezmediacore.image;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.jetbrains.annotations.NotNull;

public class StaticImage extends Image implements MapImage {

  private final BufferedImage image;

  public StaticImage(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path image,
      @NotNull final List<Integer> maps,
      @NotNull final Dimension dimension)
      throws IOException {
    super(core, image, maps, dimension);
    this.image = ImageIO.read(image.toFile());
  }

  @Override
  public void draw(final boolean resize) {
    this.onStartDrawImage();
    this.getRenderer().drawMap(this.process(this.image, resize));
    this.onFinishDrawImage();
  }
}
