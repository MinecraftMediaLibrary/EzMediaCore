package io.github.pulsebeat02.ezmediacore.callback;

import java.util.function.Consumer;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntityCallbackDispatcher extends Callback, Locatable {

  @NotNull
  Entity[] getEntities();

  @NotNull
  String getStringName();

  @Nullable
  <T> Consumer<T> modifyEntity();
}
