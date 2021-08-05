package io.github.pulsebeat02.ezmediacore.image;

import static io.github.pulsebeat02.ezmediacore.decoder.GifDecoder.GifImage;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.decoder.GifDecoder;
import io.github.pulsebeat02.ezmediacore.dimension.ImmutableDimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class DynamicImage extends Image
    implements io.github.pulsebeat02.ezmediacore.image.GifImage {

  private final GifImage image;
  private final int frameCount;
  private CompletableFuture<Void> future;
  private int frame;

  public DynamicImage(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path image,
      @NotNull final List<Integer> maps,
      @NotNull final ImmutableDimension dimension)
      throws IOException {
    super(core, image, maps, dimension);
    this.image = GifDecoder.read(new FileInputStream(image.toFile()));
    this.frameCount = this.image.getFrameCount();
  }

  @Override
  public void draw(final boolean resize) {
    this.onStartDrawImage();
    this.future =
        CompletableFuture.runAsync(
            () -> {
              for (; this.frame < this.frameCount; this.frame++) {
                this.getRenderer().drawMap(this.process(this.image.getFrame(this.frame), resize));
                try {
                  final int delay = this.image.getDelay(this.frame);
                  Thread.sleep(delay * 10L);
                } catch (final InterruptedException e) {
                  e.printStackTrace();
                }
              }
            });
    this.onFinishDrawImage();
  }

  @Override
  public void stopDrawing() {
    this.onStopDrawing();
    this.future.cancel(true);
  }

  @Override
  public void onStopDrawing() {
  }

  @Override
  public int getCurrentFrame() {
    return this.frame;
  }

  @Override
  public int getFrameCount() {
    return this.frameCount;
  }
}
