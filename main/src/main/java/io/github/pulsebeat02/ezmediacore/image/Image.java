package io.github.pulsebeat02.ezmediacore.image;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.utility.ImageUtils;
import io.github.pulsebeat02.ezmediacore.utility.ImmutableDimension;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public abstract class Image implements MapImage {

  private final MediaLibraryCore core;
  private final EnhancedMapRenderer renderer;
  private final ImmutableDimension dimension;
  private final int[][] maps;
  private final Path image;
  private final UUID uuid;

  public Image(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path image,
      final int[][] maps,
      @NotNull final ImmutableDimension dimension) {
    Preconditions.checkArgument(maps.length >= 1 && maps[0].length >= 1, "Invalid Map Matrix!");
    this.core = core;
    this.renderer = new EnhancedMapRenderer(maps);
    this.image = image;
    this.maps = maps;
    this.dimension = dimension;
    this.uuid = UUID.randomUUID();
  }

  @Override
  public void onStartDrawImage() {}

  @Override
  public @NotNull BufferedImage[][] process(@NotNull BufferedImage image, final boolean resize) {
    final int itemframeWidth = this.maps.length;
    final int itemframeHeight = this.maps[0].length;
    final int width = itemframeWidth * 128;
    final int height = itemframeHeight * 128;
    if (resize) {
      image = ImageUtils.resize(image, width, height);
    }
    final BufferedImage[][] matrix = new BufferedImage[itemframeWidth][itemframeHeight];
    for (int rows = 0; rows < matrix.length; rows++) {
      for (int cols = 0; cols < matrix[rows].length; cols++) {
        matrix[rows][cols] = new BufferedImage(128, 128, image.getType());
        ImageUtils.trimForMapSize(matrix[rows][cols], rows, cols);
      }
    }
    return matrix;
  }

  @Override
  public void onFinishDrawImage() {}

  @Override
  public int[][] getMapMatrix() {
    return this.maps;
  }

  @Override
  public @NotNull Path getImagePath() {
    return this.image;
  }

  @Override
  public @NotNull ImmutableDimension getDimensions() {
    return this.dimension;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public @NotNull MapRenderer getRenderer() {
    return this.renderer;
  }

  @Override
  public @NotNull UUID getIdentifier() {
    return uuid;
  }
}
