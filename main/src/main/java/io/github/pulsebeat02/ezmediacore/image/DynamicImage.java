package io.github.pulsebeat02.ezmediacore.image;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.decoder.GifDecoder;
import io.github.pulsebeat02.ezmediacore.utility.ImmutableDimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

import static io.github.pulsebeat02.ezmediacore.decoder.GifDecoder.GifImage;

public class DynamicImage extends ImageProvider
    implements io.github.pulsebeat02.ezmediacore.image.GifImage {

  private final GifImage image;
  private final int frameCount;
  private CompletableFuture<Void> future;
  private int frame;

  public DynamicImage(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path image,
      final int[][] maps,
      @NotNull final ImmutableDimension dimension)
      throws IOException {
    super(core, image, maps, dimension);
    this.image = GifDecoder.read(new FileInputStream(image.toFile()));
    this.frameCount = this.image.getFrameCount();
  }

  @Override
  public void draw(final boolean resize) throws IOException {
    onStartDrawImage();
    this.future =
        CompletableFuture.runAsync(
            () -> {
              for (; this.frame < this.frameCount; this.frame++) {
                getRenderer().drawMap(process(this.image.getFrame(this.frame), resize));
                try {
                  final int delay = this.image.getDelay(this.frame);
                  Thread.sleep(delay * 10L);
                } catch (final InterruptedException e) {
                  e.printStackTrace();
                }
              }
            });
    onFinishDrawImage();
  }

  @Override
  public void stopDrawing() {
    onStopDrawing();
    this.future.cancel(true);
  }

  @Override
  public void onStopDrawing() {}

  @Override
  public int getCurrentFrame() {
    return this.frame;
  }

  @Override
  public int getFrameCount() {
    return this.frameCount;
  }
}
