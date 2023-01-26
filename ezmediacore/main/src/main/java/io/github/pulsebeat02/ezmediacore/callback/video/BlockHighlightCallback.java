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
package io.github.pulsebeat02.ezmediacore.callback.video;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.entity.NamedStringCharacter;
import io.github.pulsebeat02.ezmediacore.callback.implementation.BlockHighlightCallbackDispatcher;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import java.util.UUID;
import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class BlockHighlightCallback extends FrameCallback
    implements BlockHighlightCallbackDispatcher {

  private final Location location;
  private final NamedStringCharacter character;

  BlockHighlightCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers,
      @NotNull final Dimension dimension,
      @NotNull final Location location,
      @NotNull final NamedStringCharacter character,
      @NotNull final DelayConfiguration delay) {
    super(core, viewers, dimension, delay);
    checkNotNull(location, "Location cannot be null!");
    checkNotNull(character, "Character cannot be null!");
    this.location = location;
    this.character = character;
  }

  @Override
  public void process(final int @NotNull [] data) {
    final long time = System.currentTimeMillis();
    final long delay = this.getDelayConfiguration().getDelay();
    final int z = (int) this.location.getZ();
    final Dimension dimension = this.getDimensions();
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();
    if (time - this.getLastUpdated() >= delay) {
      this.setLastUpdated(time);
      this.displayDebugMarkerScreen(width, height, z, delay, data);
    }
  }

  private void displayDebugMarkerScreen(
      final int width,
      final int height,
      final int z,
      final long delay,
      final int @NotNull [] data) {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        final UUID[] viewers = this.getWatchers().getViewers();
        final int modifiedX = (int) (this.location.getX() - (width / 2D)) + x;
        final int modifiedY = (int) (this.location.getY() + (height / 2D)) - y;
        final int color = data[width * y + x];
        final int newDelay = (int) (delay + 100);
        this.getPacketHandler()
            .displayDebugMarker(
                viewers, this.character.getCharacter(), modifiedY, z, modifiedX, color, newDelay);
      }
    }
  }

  @Override
  public @NotNull Location getLocation() {
    return this.location;
  }

  @Override
  public @NotNull NamedStringCharacter getStringName() {
    return this.character;
  }

  public static final class Builder extends VideoCallbackBuilder {

    private NamedStringCharacter character = NamedStringCharacter.NORMAL_SQUARE;
    private Location location;

    public Builder() {}

    @Contract("_ -> this")
    public @NotNull Builder location(@NotNull final Location location) {
      this.location = location;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder character(@NotNull final NamedStringCharacter character) {
      this.character = character;
      return this;
    }

    @Contract("_ -> new")
    @Override
    public @NotNull FrameCallback build(@NotNull final MediaLibraryCore core) {
      return new BlockHighlightCallback(
          core, this.getViewers(), this.getDims(), this.location, this.character, this.getDelay());
    }
  }
}
