/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.image;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.utility.graphics.GifDecoder;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class DynamicImage extends Image
    implements io.github.pulsebeat02.ezmediacore.image.GifImage {

  private static final ExecutorService MAP_UPDATE_POOL;

  static {
    MAP_UPDATE_POOL = Executors.newCachedThreadPool();
  }

  private final GifDecoder.GifImage image;
  private final int frameCount;
  private final AtomicBoolean cancelled;
  private int frame;

  public DynamicImage(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path image,
      @NotNull final List<Integer> maps,
      @NotNull final Dimension dimension)
      throws IOException {
    super(core, image, maps, dimension);
    this.image = GifDecoder.read(new FileInputStream(image.toFile()));
    this.cancelled = new AtomicBoolean(false);
    this.frameCount = this.image.getFrameCount();
  }

  @Override
  public void draw(final boolean resize) {
    this.onStartDrawImage();
    CompletableFuture.runAsync(() -> this.drawImage(resize), MAP_UPDATE_POOL);
    this.onFinishDrawImage();
  }

  private void drawImage(final boolean resize) {
    while (!this.cancelled.get()) {
      for (; this.frame < this.frameCount; this.frame++) {
        this.getRenderer().drawMap(this.process(this.image.getFrame(this.frame), resize));
        try {
          TimeUnit.MILLISECONDS.sleep(this.image.getDelay(this.frame) * 10L);
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public void stopDrawing() {
    this.onStopDrawing();
    this.cancelled.set(true);
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
