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
package io.github.pulsebeat02.ezmediacore.listener;

import static org.bukkit.event.EventPriority.NORMAL;

import java.util.function.Consumer;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

class EventExecutorListener<E extends Event> implements Listener, EventExecutor {

  final Consumer<? super E> c;

  EventExecutorListener(@NotNull final Consumer<? super E> c) {
    this.c = c;
  }

  static <E extends Event> void register(
      @NotNull final Plugin plugin,
      @NotNull final Class<E> clazz,
      @NotNull final Consumer<? super E> blah) {
    final EventExecutorListener<E> e = new EventExecutorListener<E>(blah);
    plugin.getServer().getPluginManager().registerEvent(clazz, e, NORMAL, e, plugin, false);
  }

  @Override
  public void execute(@NotNull final Listener l, @NotNull final Event e) {
    this.c.accept((E) e);
  }
}
