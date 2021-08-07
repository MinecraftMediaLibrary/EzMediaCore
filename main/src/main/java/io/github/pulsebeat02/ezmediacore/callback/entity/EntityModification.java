package io.github.pulsebeat02.ezmediacore.callback.entity;

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
    this.entityClass = clazz;
    this.methods = this.getProperMethods();
    this.validArguments =
        Set.of(
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            boolean.class,
            char.class,
            String.class);
  }

  /**
   * Gets the proper methods from the specific entity class.
   *
   * @return the map containing the method names and actual methods itself
   */
  private Map<String, Method> getProperMethods() {
    final Map<String, Method> methods = new HashMap<>();
    for (final Method method : this.entityClass.getDeclaredMethods()) {
      final Class<?>[] args = method.getParameterTypes();
      final String name = method.getName();
      if (name.startsWith("get") && args.length == 1 && this.validArguments.contains(args[0])) {
        methods.put(name, method);
      }
    }
    return methods;
  }

  @Override
  public boolean invokeGetter(
      @NotNull final T entity, @NotNull final String methodName, @NotNull final Object argument) {
    return false;
  }

  @Override
  public @NotNull Class<?> getEntityClass() {
    return this.entityClass;
  }

  @Override
  public @NotNull Map<String, Method> getMethodMap() {
    return this.methods;
  }

  @Override
  public @NotNull Set<Class<?>> getValidArguments() {
    return this.validArguments;
  }

  @Override
  public @NotNull Collection<Method> getRawMethods() {
    return this.methods.values();
  }

  @Override
  public @NotNull Collection<String> getMethodNames() {
    return this.methods.keySet();
  }
}
