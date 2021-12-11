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
import io.github.pulsebeat02.emcdependencymanagement.component.Artifact;
import io.github.pulsebeat02.emcdependencymanagement.component.Relocation;
import io.github.pulsebeat02.emcdependencymanagement.component.Repository;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class LibraryDependencyManager {

  private final MediaLibraryCore core;

  public LibraryDependencyManager(@NotNull final MediaLibraryCore core)
      throws IOException, ReflectiveOperationException {
    this.core = core;
    this.start();
  }

  private void start() throws IOException, ReflectiveOperationException {

    //    ApplicationBuilder.appending("EzMediaCore -
    // %s".formatted(this.core.getPlugin().getName()))
    //        .mirrorSelector((collection, collection1) -> collection)
    //        .downloadDirectoryPath(this.core.getDependencyPath())
    //        .internalRepositories(this.getRepo())
    //        .logger(this.getProcessLogger())
    //        .build();

    final Set<Repository> repos =
        Stream.of(
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

    final Set<Artifact> artifacts =
        Stream.of(
                ofArtifact("uk:co:caprica", "vlcj", "4:7:1"),
                ofArtifact("uk:co:caprica", "vlcj-natives", "4:5:0"),
                ofArtifact("com:github:sealedtx", "java-youtube-downloader", "3:0:2"), //
                ofArtifact("com:alibaba", "fastjson", "1:2:78"),
                ofArtifact("net:java:dev:jna", "jna", "5:10:0"),
                ofArtifact("net:java:dev:jna", "jna-platform", "5:10:0"),
                ofArtifact("se:michaelthelin:spotify", "spotify-web-api-java", "7:0:0"),
                ofArtifact("com:github:kokorin:jaffree", "jaffree", "2021:11:06"),
                ofArtifact("org:jcodec", "jcodec", "0:2:5"),
                ofArtifact("com:github:ben-manes:caffeine", "caffeine", "3:0:5"),
                ofArtifact("io:github:pulsebeat02", "emc-installers", "v1:0:1"),
                ofArtifact("it:unimi:dsi", "fastutil", "8:5:6"))
            .collect(Collectors.toSet());

    final String base = "io:github:pulsebeat02:ezmediacore:lib:%s";
    final Set<Relocation> relocations =
        Stream.of(
                ofRelocation("uk:co:caprica", base.formatted("caprica")),
                ofRelocation("com:github:kiulian", base.formatted("kiulian")),
                ofRelocation("se.michaelthelin", base.formatted("michaelthelin")),
                ofRelocation("com:github:kokorin", base.formatted("kokorin")),
                ofRelocation("org:jcodec", base.formatted("jcodec")),
                ofRelocation("com:github:benmanes", base.formatted("caffeine")),
                ofRelocation("it:unimi:dsi", base.formatted("dsi")),
                ofRelocation("com:alilbaba", base.formatted("alilbaba")),
                ofRelocation("net:sourceforge:jaad:aac", base.formatted("sourceforge")))
            .collect(Collectors.toSet());

    final EMCDepManagement management =
        EMCDepManagement.builder()
            .setApplicationName("EzMediaCore - %s".formatted(this.core.getPlugin().getName()))
            .setFolder(this.core.getDependencyPath())
            .setRepos(repos)
            .setArtifacts(artifacts)
            .setRelocations(relocations)
            .createEMCDepManagement();
    management.load();
  }

  //  @Contract(pure = true)
  //  private @NotNull ProcessLogger getProcessLogger() {
  //    return (s, objects) -> this.core.getLogger().info(s.formatted(objects));
  //  }
  //
  //  @Contract(" -> new")
  //  private @NotNull @Unmodifiable Collection<Repository> getRepo() throws MalformedURLException {
  //    return Collections.singleton(new Repository(new URL(SimpleMirrorSelector.ALT_CENTRAL_URL)));
  //  }
}
