package io.github.pulsebeat02.ezmediacore.dependency.injector;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.github.pulsebeat02.ezmediacore.utility.unsafe.UnsafeUtils.getField;

import io.github.pulsebeat02.ezmediacore.utility.unsafe.UnsafeManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

public final class UnsafeInjection {

  private static final Unsafe UNSAFE;

  static {
    UNSAFE = UnsafeManager.getUnsafe();
  }

  private final Iterable<Path> jars;
  private final Deque<URL> unopened;
  private final List<URL> paths;

  public UnsafeInjection(
      @NotNull final Iterable<Path> jars, @NotNull final URLClassLoader classloader)
      throws NoSuchFieldException {
    checkNotNull(jars, "JARs cannot be null!");
    checkNotNull(classloader, "ClassLoader cannot be null!");
    this.jars = jars;
    final Object ucp = this.getUCP(classloader);
    this.unopened = this.getUnopenedURLs(ucp);
    this.paths = this.getPathURLs(ucp);
  }

  public void inject() throws MalformedURLException {
    for (final Path path : this.jars) {
      final URL url = path.toUri().toURL();
      this.unopened.add(url);
      this.paths.add(url);
    }
  }

  @NotNull
  private ArrayList<URL> getPathURLs(final Object ucp) throws NoSuchFieldException {
    return (ArrayList<URL>) getField(ucp, "path");
  }

  @NotNull
  private ArrayDeque<URL> getUnopenedURLs(final Object ucp) throws NoSuchFieldException {
    return (ArrayDeque<URL>) getField(ucp, "unopenedUrls");
  }

  @NotNull
  private Object getUCP(@NotNull final URLClassLoader classloader) throws NoSuchFieldException {
    return getField(URLClassLoader.class, classloader, "ucp");
  }
}
