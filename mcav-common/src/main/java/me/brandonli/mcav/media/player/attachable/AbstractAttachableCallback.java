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
package me.brandonli.mcav.media.player.attachable;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicReference;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A thread-safe {@link AttachableCallback} that stores its value in an atomic reference.
 *
 * @param <ValueType> the type of value the slot holds
 */
public abstract class AbstractAttachableCallback<ValueType> implements AttachableCallback<ValueType> {

  private final ValueType fallback;
  private final AtomicReference<@Nullable ValueType> attached;

  /**
   * Constructs a slot.
   *
   * @param fallback the value returned while nothing is attached
   * @throws NullPointerException if {@code fallback} is null
   */
  protected AbstractAttachableCallback(final ValueType fallback) {
    Preconditions.checkNotNull(fallback, "Fallback must not be null!");
    this.fallback = fallback;
    this.attached = new AtomicReference<>();
  }

  /**
   * {@inheritDoc}
   * @throws NullPointerException if {@code value} is null
   */
  @Override
  public void attach(final ValueType value) {
    Preconditions.checkNotNull(value, "Attached value must not be null!");
    this.attached.set(value);
  }

  @Override
  public void detach() {
    this.attached.set(null);
  }

  @Override
  public boolean isAttached() {
    final ValueType current = this.attached.get();
    return current != null;
  }

  @Override
  public ValueType retrieve() {
    final ValueType current = this.attached.get();
    return current == null ? this.fallback : current;
  }
}
