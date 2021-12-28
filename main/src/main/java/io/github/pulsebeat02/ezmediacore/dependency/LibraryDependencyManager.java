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
package io.github.pulsebeat02.ezmediacore.dependency;

import static io.github.pulsebeat02.emcdependencymanagement.component.Artifact.ofArtifact;
import static io.github.pulsebeat02.emcdependencymanagement.component.Relocation.ofRelocation;

import io.github.pulsebeat02.emcdependencymanagement.EMCDepManagement;
import io.github.pulsebeat02.emcdependencymanagement.SimpleLogger;
import io.github.pulsebeat02.emcdependencymanagement.component.Artifact;
import io.github.pulsebeat02.emcdependencymanagement.component.Relocation;
import io.github.pulsebeat02.emcdependencymanagement.component.Repository;
import io.github.pulsebeat02.ezmediacore.CoreLogger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class LibraryDependencyManager extends LibraryDependency {

  public LibraryDependencyManager(@NotNull final MediaLibraryCore core)
      throws IOException, ReflectiveOperationException {
    super(core);
  }

  @Override
  public void start() throws IOException {

    final MediaLibraryCore core = this.getCore();
    final Set<Repository> repos = this.getRepositories();
    final Set<Artifact> artifacts = this.getArtifacts();
    final Set<Relocation> relocations = this.getRelocations();

    final EMCDepManagement management =
        this.constructApplication(core, repos, artifacts, relocations);

    try {
      management.load();
    } catch (final ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private EMCDepManagement constructApplication(
      @NotNull final MediaLibraryCore core,
      @NotNull final Set<Repository> repos,
      @NotNull final Set<Artifact> artifacts,
      @NotNull final Set<Relocation> relocations)
      throws IOException {
    return EMCDepManagement.builder()
        .setApplicationName("EzMediaCore - %s".formatted(core.getPlugin().getName()))
        .setFolder(core.getDependencyPath())
        .setRepos(repos)
        .setArtifacts(artifacts)
        .setRelocations(relocations)
        .setLogger(this.createLogger())
        .create();
  }

  @NotNull
  private Set<Relocation> getRelocations() {
    final String base = "io:github:pulsebeat02:ezmediacore:lib:%s";
    return Stream.of(
            ofRelocation("uk:co:caprica", base.formatted("caprica")),
            ofRelocation("com:github:kiulian", base.formatted("kiulian")),
            ofRelocation("se.michaelthelin", base.formatted("michaelthelin")),
            ofRelocation("com:github:kokorin", base.formatted("kokorin")),
            ofRelocation("org:jcodec", base.formatted("jcodec")),
            ofRelocation("com:github:benmanes", base.formatted("benmanes")),
            ofRelocation("it:unimi:dsi", base.formatted("dsi")),
            ofRelocation("com:alibaba", base.formatted("alibaba")),
            ofRelocation("net:sourceforge:jaad:aac", base.formatted("sourceforge")),
            ofRelocation("com:fasterxml", base.formatted("fasterxml")),
            ofRelocation("org:apache:httpcomponents", base.formatted("apache:httpcomponents")),
            ofRelocation("com:neovisionaries", base.formatted("neovisionaries")))
        .collect(Collectors.toSet());
  }

  @NotNull
  private Set<Artifact> getArtifacts() {
    return Stream.of(
            ofArtifact("uk:co:caprica", "vlcj", "4:7:1"),
            ofArtifact("uk:co:caprica", "vlcj-natives", "4:5:0"),
            ofArtifact("com:github:sealedtx", "java-youtube-downloader", "3:0:2"),
            ofArtifact("com:alibaba", "fastjson", "1:2:78"),
            ofArtifact("net:java:dev:jna", "jna", "5:10:0"),
            ofArtifact("net:java:dev:jna", "jna-platform", "5:10:0"),
            ofArtifact("se:michaelthelin:spotify", "spotify-web-api-java", "7:0:0"),
            ofArtifact("com:github:kokorin:jaffree", "jaffree", "2021:11:06"),
            ofArtifact("org:jcodec", "jcodec", "0:2:5"),
            ofArtifact("com:github:ben-manes:caffeine", "caffeine", "3:0:5"),
            ofArtifact("it:unimi:dsi", "fastutil", "8:5:6"),
            ofArtifact("com:fasterxml:jackson:core", "jackson-core", "2:13:0"),
            ofArtifact("org:apache:httpcomponents:client5", "httpclient5", "5.2-alpha1"),
            ofArtifact("com:neovisionaries", "nv-i18n", "1:29"))
        .collect(Collectors.toSet());
  }

  @NotNull
  private Set<Repository> getRepositories() {
    return Stream.of(
            "https://papermc.io/repo/repository/maven-public/",
            "https://oss.sonatype.org/content/repositories/snapshots",
            "https://libraries.minecraft.net/",
            "https://jitpack.io/",
            "https://repo.codemc.org/repository/maven-public/",
            "https://m2.dv8tion.net/releases/",
            "https://repo.vshnv.tech/releases/",
            "https://repo.mattstudios.me/artifactory/public/",
            "https://pulsebeat02.jfrog.io/artifactory/pulse-gradle-release-local/",
            "https://pulsebeat02.jfrog.io/artifactory/pulse-libs-release-local/")
        .map(Repository::ofRepo)
        .collect(Collectors.toSet());
  }

  @Override
  public void onInstallation(@NotNull final Path path) {}

  private @NotNull SimpleLogger createLogger() {
    return new SimpleLogger() {

      private final CoreLogger logger;

      {
        this.logger = LibraryDependencyManager.this.getCore().getLogger();
      }

      @Override
      public void info(@NotNull final String line) {
        this.logger.info(line);
      }

      @Override
      public void warning(@NotNull final String line) {
        this.logger.warn(line);
      }

      @Override
      public void error(@NotNull final String line) {
        this.logger.error(line);
      }
    };
  }
}
