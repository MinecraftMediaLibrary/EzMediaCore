/*
 * This file is part of mcav, a media playback library for Java
 * Copyright (C) Brandon Li <https://brandonli.me/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.brandonli.mcav.installer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.brandonli.mcav.installer.testing.LocalMavenRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link MCAVInstaller} against a Maven repository on the file system.
 */
final class MCAVInstallerTest {

  private static final String SNAPSHOT = "1.0.0-SNAPSHOT";
  private static final List<String> NO_DEPENDENCIES = List.of();

  private static final Pattern LOADED_MESSAGE = Pattern.compile("Loaded 0 jars for helper in (\\d+) ms\\.");
  private static final int ELAPSED_GROUP = 1;

  @TempDir
  private Path directory;

  private LocalMavenRepository repository;
  private Path folder;

  /**
   * A jar loader that remembers what it was asked to load.
   */
  private static final class RecordingLoader implements JarLoader {

    private final List<Path> jars = new ArrayList<>();
    private final List<ClassLoader> loaders = new ArrayList<>();

    @Override
    public void loadJars(final Collection<Path> jars, final ClassLoader loader) {
      this.jars.addAll(jars);
      this.loaders.add(loader);
    }

    private boolean isEmpty() {
      final boolean noJars = this.jars.isEmpty();
      final boolean noLoaders = this.loaders.isEmpty();
      return noJars && noLoaders;
    }
  }

  @BeforeEach
  void publishTheModules() throws IOException {
    final Path remote = this.directory.resolve("remote");
    this.repository = new LocalMavenRepository(remote);
    this.folder = this.directory.resolve("dependencies");

    final String helper = LocalMavenRepository.dependency("me.test", "helper", "1.0", "compile", false);
    final List<String> commonDependencies = List.of(helper);
    this.repository.publish(Artifact.GROUP_ID, "mcav-common", SNAPSHOT, commonDependencies);
    this.repository.publish(Artifact.GROUP_ID, "mcav-bukkit", SNAPSHOT, NO_DEPENDENCIES);
    this.repository.publish("me.test", "helper", "1.0", NO_DEPENDENCIES);
  }

  private MCAVInstaller createInstaller(final ClassLoader classLoader) {
    final String url = this.repository.getUrl();
    final RemoteRepository remote = InstallationManager.createRepository("test", url);
    final List<RemoteRepository> repositories = List.of(remote);
    final Path localRepository = this.directory.resolve("local");
    return new MCAVInstaller(this.folder, classLoader, target -> new InstallationManager(target, repositories, localRepository));
  }

  private static String readResource(final URLClassLoader loader, final String name) throws IOException {
    try (final InputStream stream = loader.getResourceAsStream(name)) {
      assertNotNull(stream, "missing resource " + name);
      final byte[] bytes = stream.readAllBytes();
      return new String(bytes, StandardCharsets.UTF_8);
    }
  }

  private static List<String> fileNames(final List<Path> files) {
    final List<String> names = new ArrayList<>();
    for (final Path file : files) {
      final Path name = file.getFileName();
      final String text = name.toString();
      names.add(text);
    }
    return names;
  }

  @Test
  void shouldKeepTheFolderAndClassLoaderOfTheInjector() {
    final ClassLoader loader = MCAVInstallerTest.class.getClassLoader();
    final MCAVInstaller installer = MCAVInstaller.injector(this.folder, loader);
    final Path installerFolder = installer.getFolder();
    final ClassLoader installerLoader = installer.getClassLoader();
    assertSame(this.folder, installerFolder);
    assertSame(loader, installerLoader);
  }

  @Test
  void shouldInjectIntoTheClassLoaderOfTheOwnersClass() {
    final Object owner = this;
    final MCAVInstaller installer = MCAVInstaller.injector(this.folder, owner);
    final ClassLoader installerLoader = installer.getClassLoader();
    final ClassLoader expected = MCAVInstallerTest.class.getClassLoader();
    assertSame(expected, installerLoader);
  }

  @Test
  void shouldRejectInjectorOwnersOfTheBootstrapClassLoader() {
    final Object owner = "a string, loaded by the bootstrap class loader";
    final JarInjectorException exception = assertThrows(JarInjectorException.class, () -> MCAVInstaller.injector(this.folder, owner));
    final String message = exception.getMessage();
    assertEquals("Classes of the bootstrap class loader cannot receive jars!", message);
  }

  @Test
  void shouldRejectNullInjectorArguments() {
    final ClassLoader loader = MCAVInstallerTest.class.getClassLoader();
    final Object owner = this;
    assertThrows(NullPointerException.class, () -> MCAVInstaller.injector(null, loader));
    assertThrows(NullPointerException.class, () -> MCAVInstaller.injector(this.folder, null));
    assertThrows(NullPointerException.class, () -> MCAVInstaller.injector(this.folder, (Object) null));
    assertThrows(NullPointerException.class, () -> MCAVInstaller.injector(null, owner));
  }

  @Test
  void shouldLoadAModuleWithItsDependenciesIntoAUrlClassLoader() throws IOException {
    try (final URLClassLoader loader = new URLClassLoader(new URL[0], null)) {
      final MCAVInstaller installer = this.createInstaller(loader);
      installer.loadMCAVDependencies(Artifact.COMMON);
      final String commonName = LocalMavenRepository.resourceName("mcav-common");
      final String helperName = LocalMavenRepository.resourceName("helper");
      final String common = readResource(loader, commonName);
      final String helper = readResource(loader, helperName);
      assertEquals("me.brandonli:mcav-common:1.0.0-SNAPSHOT", common);
      assertEquals("me.test:helper:1.0", helper);
    }
    final Path copied = this.folder.resolve("mcav-common/me.brandonli/mcav-common/mcav-common-1.0.0-SNAPSHOT.jar");
    final boolean exists = Files.isRegularFile(copied);
    assertTrue(exists);
  }

  @Test
  void shouldLoadAModuleWithACustomLoader() {
    final ClassLoader classLoader = MCAVInstallerTest.class.getClassLoader();
    final MCAVInstaller installer = this.createInstaller(classLoader);
    final RecordingLoader recording = new RecordingLoader();
    installer.loadMCAVDependencies(Artifact.BUKKIT, recording);
    final List<String> names = fileNames(recording.jars);
    final List<String> expectedNames = List.of("mcav-bukkit-1.0.0-SNAPSHOT.jar");
    final List<ClassLoader> expectedLoaders = List.of(classLoader);
    assertEquals(expectedNames, names);
    assertEquals(expectedLoaders, recording.loaders);
  }

  @Test
  void shouldLoadAnyArtifactAndReturnTheAddedJars() {
    final ClassLoader classLoader = MCAVInstallerTest.class.getClassLoader();
    final MCAVInstaller installer = this.createInstaller(classLoader);
    final RecordingLoader recording = new RecordingLoader();
    final List<Path> jars = installer.loadDependencies("me.test", "helper", "1.0", recording);
    final List<String> names = fileNames(jars);
    final List<String> expectedNames = List.of("helper-1.0.jar");
    assertEquals(expectedNames, names);
    assertEquals(jars, recording.jars);

    final Path expectedFolder = this.folder.resolve("helper/me.test/helper");
    final Path first = jars.getFirst();
    final Path parent = first.getParent();
    assertEquals(expectedFolder, parent);
  }

  @Test
  void shouldNotLoadAnythingWhenTheDownloadFails() {
    final ClassLoader classLoader = MCAVInstallerTest.class.getClassLoader();
    final MCAVInstaller installer = this.createInstaller(classLoader);
    final RecordingLoader recording = new RecordingLoader();
    assertThrows(InstallationException.class, () -> installer.loadDependencies("me.test", "missing", "1.0", recording));
    final boolean nothingLoaded = recording.isEmpty();
    assertTrue(nothingLoaded);
  }

  @Test
  void shouldReportElapsedInstallationTimeRatherThanEpochTime() {
    final ClassLoader classLoader = MCAVInstallerTest.class.getClassLoader();
    final InstallationManager manager = mock(InstallationManager.class);
    final List<Path> downloaded = List.of();
    when(manager.downloadDependencies("me.test", "helper", "1.0")).thenReturn(downloaded);
    final MCAVInstaller installer = new MCAVInstaller(this.folder, classLoader, _ -> manager);
    final RecordingLoader recording = new RecordingLoader();
    final String loggerName = MCAVInstaller.class.getName();
    final Logger logger = Logger.getLogger(loggerName);
    final RecordingHandler handler = new RecordingHandler();
    logger.addHandler(handler);
    final long before = System.currentTimeMillis();
    final List<Path> jars = installer.loadDependencies("me.test", "helper", "1.0", recording);
    final long after = System.currentTimeMillis();
    logger.removeHandler(handler);

    final List<String> messages = handler.getMessages();
    final String message = messages.stream().filter(LOADED_MESSAGE.asMatchPredicate()).findFirst().orElseThrow();
    final Matcher matcher = LOADED_MESSAGE.matcher(message);
    final boolean loaded = matcher.matches();
    assertTrue(loaded, message);
    final String elapsedText = matcher.group(ELAPSED_GROUP);
    final long elapsed = Long.parseLong(elapsedText);
    final long total = after - before;
    assertTrue(elapsed >= 0 && elapsed <= total, "reported duration must fit within the observed call duration");
    assertSame(downloaded, jars);
    verify(manager).close();
  }

  @Test
  void shouldRejectNullArguments() {
    final ClassLoader classLoader = MCAVInstallerTest.class.getClassLoader();
    final MCAVInstaller installer = this.createInstaller(classLoader);
    final RecordingLoader recording = new RecordingLoader();
    assertThrows(NullPointerException.class, () -> installer.loadMCAVDependencies(null));
    assertThrows(NullPointerException.class, () -> installer.loadMCAVDependencies(null, recording));
    assertThrows(NullPointerException.class, () -> installer.loadMCAVDependencies(Artifact.COMMON, null));
    assertThrows(NullPointerException.class, () -> installer.loadDependencies(null, "helper", "1.0", recording));
    assertThrows(NullPointerException.class, () -> installer.loadDependencies("me.test", null, "1.0", recording));
    assertThrows(NullPointerException.class, () -> installer.loadDependencies("me.test", "helper", null, recording));
    assertThrows(NullPointerException.class, () -> installer.loadDependencies("me.test", "helper", "1.0", null));
    final boolean nothingLoaded = recording.isEmpty();
    assertTrue(nothingLoaded);
  }

  private static final class RecordingHandler extends Handler {

    private final List<String> messages;

    private RecordingHandler() {
      this.messages = new CopyOnWriteArrayList<>();
    }

    @Override
    public void publish(final LogRecord logRecord) {
      final String message = logRecord.getMessage();
      this.messages.add(message);
    }

    @Override
    public void flush() {}

    @Override
    public void close() {}

    private List<String> getMessages() {
      return this.messages;
    }
  }
}
