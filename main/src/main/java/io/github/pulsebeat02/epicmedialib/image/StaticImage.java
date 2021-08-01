package io.github.pulsebeat02.epicmedialib.image;

import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import io.github.pulsebeat02.epicmedialib.utility.ImmutableDimension;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.jetbrains.annotations.NotNull;

public class StaticImage extends ImageProvider implements MapImage {

  public StaticImage(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path image,
      final int[][] maps,
      @NotNull final ImmutableDimension dimension) {
    super(core, image, maps, dimension);
  }

  @Override
  public void draw(final boolean resize) throws IOException {
    onStartDrawImage();
    getRenderer().drawMap(process(ImageIO.read(this.getImagePath().toFile()), resize));
    onFinishDrawImage();
  }
}
