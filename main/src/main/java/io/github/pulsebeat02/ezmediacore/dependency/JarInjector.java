package io.github.pulsebeat02.ezmediacore.dependency;

import io.github.slimjar.injector.DependencyInjector;
import io.github.slimjar.injector.loader.Injectable;
import io.github.slimjar.resolver.data.DependencyData;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public final class JarInjector implements DependencyInjector {

  private final Collection<URL> dependencies;

  JarInjector(final Collection<URL> dependencies) {
    this.dependencies = dependencies;
  }

  @Override
  public void inject(@NotNull final Injectable injectable, @NotNull final DependencyData data) {
    this.dependencies.forEach(
        x -> {
          try {
            injectable.inject(x);
          } catch (final InvocationTargetException
              | IllegalAccessException
              | IOException
              | URISyntaxException e) {
            e.printStackTrace();
          }
        });
  }
}
