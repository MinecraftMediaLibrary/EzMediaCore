package io.github.pulsebeat02.epicmedialib.dependency;

import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.resolver.data.DependencyData;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;

public final class JarLoader {

  private final Collection<URL> urls;

  public JarLoader(@NotNull final Collection<Path> paths) {
    this.urls = new ArrayList<>();
    paths.forEach(
        url -> {
          try {
            this.urls.add(new URL(url.toUri().toString().replace("\\", "/").replace("%20", " ")));
          } catch (final MalformedURLException e) {
            e.printStackTrace();
          }
        });
  }

  public void inject() {
    try {
      ApplicationBuilder.appending("EpicMediaLib")
          .injectorFactory(helper -> new JarInjector(this.urls))
          .dataProviderFactory(
              (url) ->
                  () ->
                      new DependencyData(
                          Collections.emptyList(),
                          Collections.emptyList(),
                          Collections.emptyList(),
                          Collections.emptyList()))
          .build();
    } catch (final URISyntaxException
        | ReflectiveOperationException
        | NoSuchAlgorithmException
        | IOException e) {
      e.printStackTrace();
    }
  }
}
