/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.media.player.pipeline.step;

import com.google.common.base.Preconditions;
import java.util.Optional;
import me.brandonli.mcav.media.image.ImageBuffer;
import me.brandonli.mcav.media.player.metadata.OriginalVideoMetadata;
import me.brandonli.mcav.media.player.pipeline.filter.video.VideoFilter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The default {@link VideoPipelineStep}.
 *
 * <p>This class is immutable; it is as thread-safe as its filters.
 *
 * @since 1.0.0
 */
public final class DefaultVideoPipelineStep implements VideoPipelineStep {

  private final VideoFilter filter;
  private final @Nullable VideoPipelineStep next;

  DefaultVideoPipelineStep(final @Nullable VideoPipelineStep next, final VideoFilter filter) {
    this.next = next;
    this.filter = filter;
  }

  /**
   * Returns the video step that follows this one, or an empty optional when this step is the last.
   *
   * @return the next step, or empty if this is the last step
   * @since 1.0.0
   */
  @Override
  public Optional<VideoPipelineStep> next() {
    return Optional.ofNullable(this.next);
  }

  /**
   * Applies the filter of this step to the frame. The filter's return value is ignored; this call does not invoke the next step.
   *
   * @param buffer   the frame
   * @param metadata the metadata of the original video
   * @throws NullPointerException if the frame or the metadata is null
   * @since 1.0.0
   */
  @Override
  public void process(final ImageBuffer buffer, final OriginalVideoMetadata metadata) {
    Preconditions.checkNotNull(buffer, "Buffer must not be null!");
    Preconditions.checkNotNull(metadata, "Metadata must not be null!");

    this.filter.applyFilter(buffer, metadata);
  }

  /**
   * Returns the video filter this step was created with.
   *
   * @return the filter
   * @since 1.0.0
   */
  @Override
  public VideoFilter getFilter() {
    return this.filter;
  }
}
