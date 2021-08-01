package io.github.pulsebeat02.epicmedialib.entity;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface EntityCommandMutatorHelper<T extends Entity> {

  boolean invokeGetter(
      @NotNull final T entity, @NotNull final String methodName, @NotNull final Object argument);

  @NotNull
  Class<?> getEntityClass();

  @NotNull
  Map<String, Method> getMethodMap();

  @NotNull
  Set<Class<?>> getValidArguments();

  @NotNull
  Collection<Method> getRawMethods();

  @NotNull
  Collection<String> getMethodNames();
}
