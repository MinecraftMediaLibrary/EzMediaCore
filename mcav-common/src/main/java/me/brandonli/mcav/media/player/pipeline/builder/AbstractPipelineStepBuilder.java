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
package me.brandonli.mcav.media.player.pipeline.builder;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import me.brandonli.mcav.media.player.pipeline.filter.Filter;
import me.brandonli.mcav.media.player.pipeline.step.PipelineStep;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class AbstractPipelineStepBuilder<
  DataType,
  MetadataType,
  FilterType extends Filter<DataType, MetadataType>,
  StepType extends PipelineStep<DataType, MetadataType, StepType>,
  BuilderType
> {

  private final List<FilterType> filters;

  AbstractPipelineStepBuilder() {
    this.filters = new ArrayList<>();
  }

  public BuilderType then(final FilterType filter) {
    Preconditions.checkNotNull(filter, "Filter must not be null!");

    this.filters.add(filter);
    return this.self();
  }

  public int size() {
    return this.filters.size();
  }

  public StepType build() {
    final int count = this.filters.size();
    if (count == 0) {
      return this.createEmptyStep();
    }

    final int lastIndex = count - 1;
    final FilterType last = this.filters.get(lastIndex);
    StepType chain = this.createStep(null, last);
    for (int filterIndex = lastIndex - 1; filterIndex >= 0; filterIndex--) {
      final FilterType filter = this.filters.get(filterIndex);
      chain = this.createStep(chain, filter);
    }

    return chain;
  }

  abstract StepType createStep(final @Nullable StepType next, final FilterType filter);

  abstract StepType createEmptyStep();

  abstract BuilderType self();
}
