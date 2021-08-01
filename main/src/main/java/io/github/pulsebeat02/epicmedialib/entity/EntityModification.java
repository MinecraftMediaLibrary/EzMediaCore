package io.github.pulsebeat02.epicmedialib.entity;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class EntityModification<T extends Entity> implements EntityCommandMutatorHelper<T> {

  private final Class<?> entityClass;
  private final Map<String, Method> methods;
  private final Set<Class<?>> validArguments;

  /**
   * Instantiates a new EntityModificationHelper.
   *
   * @param clazz the entity type
   */
  public EntityModification(@NotNull final Class<?> clazz) {
    entityClass = clazz;
    methods = getProperMethods();
    validArguments =
        ImmutableSet.<Class<?>>builder()
            .add(byte.class)
            .add(short.class)
            .add(int.class)
            .add(long.class)
            .add(float.class)
            .add(double.class)
            .add(boolean.class)
            .add(char.class)
            .add(String.class)
            .build();
  }

  /**
   * Gets the proper methods from the specific entity class.
   *
   * @return the map containing the method names and actual methods itself
   */
  private Map<String, Method> getProperMethods() {
    final Map<String, Method> methods = new HashMap<>();
    for (final Method method : entityClass.getDeclaredMethods()) {
      final Class<?>[] args = method.getParameterTypes();
      final String name = method.getName();
      if (name.startsWith("get") && args.length == 1 && validArguments.contains(args[0])) {
        methods.put(name, method);
      }
    }
    return methods;
  }

  @Override
  public boolean invokeGetter(
      @NotNull T entity, @NotNull String methodName, @NotNull Object argument) {
    return false;
  }

  @Override
  public @NotNull Class<?> getEntityClass() {
    return entityClass;
  }

  @Override
  public @NotNull Map<String, Method> getMethodMap() {
    return methods;
  }

  @Override
  public @NotNull Set<Class<?>> getValidArguments() {
    return validArguments;
  }

  @Override
  public @NotNull Collection<Method> getRawMethods() {
    return methods.values();
  }

  @Override
  public @NotNull Collection<String> getMethodNames() {
    return methods.keySet();
  }
}
