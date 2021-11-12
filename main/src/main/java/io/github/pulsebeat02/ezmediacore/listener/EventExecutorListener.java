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
