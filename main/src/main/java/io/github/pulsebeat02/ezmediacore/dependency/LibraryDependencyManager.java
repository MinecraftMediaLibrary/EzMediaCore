package io.github.pulsebeat02.ezmediacore.dependency;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.resolver.data.Repository;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class LibraryDependencyManager {

  private final MediaLibraryCore core;

  public LibraryDependencyManager(@NotNull final MediaLibraryCore core)
      throws ReflectiveOperationException, URISyntaxException, NoSuchAlgorithmException, IOException {
    this.core = core;
    this.start();
  }

  private void start()
      throws ReflectiveOperationException, URISyntaxException, NoSuchAlgorithmException, IOException {
    ApplicationBuilder.appending("EzMediaCore - %s".formatted(this.core.getPlugin().getName()))
        .mirrorSelector((collection, collection1) -> collection)
        .downloadDirectoryPath(this.core.getDependencyPath())
        .internalRepositories(this.getRepositories())
        .logger((s, objects) -> Logger.info(s))
        .build();
  }

  private @NotNull Set<Repository> getRepositories() {
    final Set<String> repos = Set.of(
        "https://repo1.maven.org/maven2",
        "https://repo.maven.apache.org/maven2/",
        "https://jitpack.io",
        "https://m2.dv8tion.net/releases");
    return repos.stream().map(this::getRepository).collect(Collectors.toUnmodifiableSet());
  }

  private @NotNull Repository getRepository(final String str) {
    try {
      return new Repository(new URL(str));
    } catch (final MalformedURLException e) {
      e.printStackTrace();
    }
    throw new AssertionError("Repository is down! Check connection?");
  }
}
