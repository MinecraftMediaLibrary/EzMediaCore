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

package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.entity.NamedEntityString;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.FloydDither;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public abstract class CallbackFactory {

  private DelayConfiguration delay = DelayConfiguration.ofDelay(0);
  private Dimension dims;
  private Viewers viewers = Viewers.onlinePlayers();

  public @NotNull CallbackFactory delay(@NotNull final DelayConfiguration delay) {
    this.delay = delay;
    return this;
  }

  public @NotNull CallbackFactory dims(@NotNull final Dimension dims) {
    this.dims = dims;
    return this;
  }

  public @NotNull CallbackFactory viewers(@NotNull final Viewers viewers) {
    this.viewers = viewers;
    return this;
  }

  public @NotNull DelayConfiguration getDelay() {
    return this.delay;
  }

  public @NotNull Dimension getDims() {
    return this.dims;
  }

  public @NotNull Viewers getViewers() {
    return this.viewers;
  }

  public abstract @NotNull FrameCallback build(@NotNull final MediaLibraryCore core);

  public static @NotNull BlockHighlightFactory blockHighlight() {
    return new BlockHighlightFactory();
  }

  public static @NotNull ChatFactory chat() {
    return new ChatFactory();
  }

  public static @NotNull EntityFactory entity() {
    return new EntityFactory();
  }

  public static @NotNull MapFactory map() {
    return new MapFactory();
  }

  public static @NotNull ScoreboardFactory scoreboard() {
    return new ScoreboardFactory();
  }

  public static final class BlockHighlightFactory extends CallbackFactory {

    private Location location;

    public @NotNull BlockHighlightFactory location(@NotNull final Location location) {
      this.location = location;
      return this;
    }

    @Override
    public @NotNull FrameCallback build(@NotNull final MediaLibraryCore core) {
      return new BlockHighlightCallback(
          core, this.getViewers(), this.getDims(), this.location, this.getDelay());
    }
  }

  public static final class ChatFactory extends CallbackFactory {

    private NamedEntityString character = NamedEntityString.NORMAL_SQUARE;

    public @NotNull ChatFactory character(@NotNull final NamedEntityString character) {
      this.character = character;
      return this;
    }

    @Override
    public @NotNull FrameCallback build(@NotNull final MediaLibraryCore core) {
      return new ChatCallback(
          core, this.getViewers(), this.getDims(), this.character, this.getDelay());
    }
  }

  public static final class EntityFactory extends CallbackFactory {

    private NamedEntityString character = NamedEntityString.NORMAL_SQUARE;
    private Location location;
    private EntityType type = EntityType.ARMORSTAND;

    public @NotNull EntityFactory location(@NotNull final Location location) {
      this.location = location;
      return this;
    }

    public @NotNull EntityFactory character(@NotNull final NamedEntityString character) {
      this.character = character;
      return this;
    }

    public @NotNull EntityFactory type(@NotNull final EntityType type) {
      this.type = type;
      return this;
    }

    @Override
    public @NotNull FrameCallback build(@NotNull final MediaLibraryCore core) {
      return new EntityCallback(
          core,
          this.getViewers(),
          this.getDims(),
          this.location,
          this.character,
          this.type,
          this.getDelay());
    }
  }

  public static final class MapFactory extends CallbackFactory {

    private DitherAlgorithm algorithm = new FloydDither();
    private Identifier<Integer> map = Identifier.ofIdentifier(0);
    private int blockWidth;

    public @NotNull MapFactory algorithm(@NotNull final DitherAlgorithm algorithm) {
      this.algorithm = algorithm;
      return this;
    }

    public @NotNull MapFactory map(@NotNull final Identifier<Integer> id) {
      this.map = id;
      return this;
    }

    public @NotNull MapFactory blockWidth(final int blockWidth) {
      this.blockWidth = blockWidth;
      return this;
    }

    @Override
    public @NotNull FrameCallback build(@NotNull final MediaLibraryCore core) {
      return new MapCallback(
          core,
          this.getViewers(),
          this.getDims(),
          this.algorithm,
          this.map,
          this.getDelay(),
          this.blockWidth);
    }
  }

  public static final class ScoreboardFactory extends CallbackFactory {

    private Identifier<Integer> id;

    public @NotNull ScoreboardFactory id(@NotNull final Identifier<Integer> id) {
      this.id = id;
      return this;
    }

    @Override
    public @NotNull FrameCallback build(@NotNull final MediaLibraryCore core) {
      return new ScoreboardCallback(
          core, this.getViewers(), this.getDims(), this.getDelay(), this.id);
    }
  }
}
