/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
import static java.util.Objects.requireNonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.pulsebeat02.emcdependencymanagement.EMCDepManagement;
import io.github.pulsebeat02.emcdependencymanagement.SimpleLogger;
import io.github.pulsebeat02.emcdependencymanagement.component.Artifact;
import io.github.pulsebeat02.emcdependencymanagement.component.Relocation;
import io.github.pulsebeat02.emcdependencymanagement.component.Repository;
import rewrite.logging.Logger;
import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Set;

import rewrite.json.GsonProvider;
import io.github.pulsebeat02.ezmediacore.utility.io.ResourceUtils;


public final class LibraryDependencyManager extends LibraryDependency {

  private final Gson gson;

  public LibraryDependencyManager( final EzMediaCore core) throws IOException {
    super(core);
    this.gson = GsonProvider.getSimple();
  }

  @Override
  public void start() throws IOException {

    final EzMediaCore core = this.getCore();
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
       final EzMediaCore core,
       final Set<Repository> repos,
       final Set<Artifact> artifacts,
       final Set<Relocation> relocations)
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

  
  private Set<Relocation> getRelocations() throws IOException {
    try (final Reader reader =
        ResourceUtils.getResourceAsInputStream("/emc-json/library/relocations.json")) {
      final TypeToken<Set<Relocation>> token = new TypeToken<>() {};
      return this.gson.fromJson(reader, token.getType());
    }
  }

  
  private Set<Artifact> getArtifacts() throws IOException {
    try (final Reader reader =
        ResourceUtils.getResourceAsInputStream("/emc-json/library/dependencies.json")) {
      final TypeToken<Set<Artifact>> token = new TypeToken<>() {};
      return this.gson.fromJson(reader, token.getType());
    }
  }

  
  private Set<Repository> getRepositories() throws IOException {
    try (final Reader reader =
        ResourceUtils.getResourceAsInputStream("/emc-json/library/repositories.json")) {
      final TypeToken<Set<Repository>> token = new TypeToken<>() {};
      return this.gson.fromJson(reader, token.getType());
    }
  }

  @Override
  public void onInstallation( final Path path) {}

  private  SimpleLogger createLogger() {
    return new SimpleLogger() {

      private final Logger logger;

      {
        this.logger = LibraryDependencyManager.this.getCore().getLogger();
      }

      @Override
      public void info( final String line) {
        this.logger.info(line);
      }

      @Override
      public void warning( final String line) {
        this.logger.warn(line);
      }

      @Override
      public void error( final String line) {
        this.logger.error(line);
      }
    };
  }
}
