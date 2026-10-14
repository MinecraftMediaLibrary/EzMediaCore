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
package me.brandonli.mcav.capability.installer.vlc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class PreloadingVlcDiscoveryTest {

  private static final Path LIBRARY_DIRECTORY = Path.of("installation", "usr", "lib", "aarch64-linux-gnu");

  private static final String PRELOAD = "preload";
  private static final String SYSTEM = "system";
  private static final String LOAD = "load";

  private List<String> calls;
  private FakeDiscovery delegate;
  private PreloadingVlcDiscovery discovery;

  @BeforeEach
  void setUp() {
    this.calls = new ArrayList<>();
    this.delegate = new FakeDiscovery(this.calls);
    this.discovery = new PreloadingVlcDiscovery(this::preload, this.delegate);
  }

  @Test
  void shouldPreloadBeforeLoadingTheInstalledLibraries() {
    this.delegate.found = true;

    final boolean loaded = this.discovery.loadInstalledLibraries(LIBRARY_DIRECTORY);
    final List<String> expected = List.of(PRELOAD, LOAD);
    assertTrue(loaded);
    assertEquals(expected, this.calls);
    assertEquals(LIBRARY_DIRECTORY, this.delegate.loadedDirectory);
  }

  @Test
  void shouldReportInstalledLibrariesThatDoNotLoad() {
    final boolean loaded = this.discovery.loadInstalledLibraries(LIBRARY_DIRECTORY);
    assertFalse(loaded);
  }

  @Test
  void shouldDiscoverTheSystemInstallationWithoutPreloading() {
    this.delegate.found = true;

    final boolean found = this.discovery.discoverSystemInstallation();
    final List<String> expected = List.of(SYSTEM);
    assertTrue(found);
    assertEquals(expected, this.calls);
  }

  @Test
  void shouldReportAMissingSystemInstallation() {
    final boolean found = this.discovery.discoverSystemInstallation();
    assertFalse(found);
  }

  @Test
  void shouldReportTheDiscoveredPathOfTheDelegate() {
    this.delegate.discoveredPath = LIBRARY_DIRECTORY;

    final @Nullable Path discoveredPath = this.discovery.getDiscoveredPath();
    assertEquals(LIBRARY_DIRECTORY, discoveredPath);
  }

  @Test
  void shouldReportNoPathWhenTheDelegateHasNone() {
    final @Nullable Path discoveredPath = this.discovery.getDiscoveredPath();
    assertNull(discoveredPath);
  }

  private void preload() {
    this.calls.add(PRELOAD);
  }

  private static final class FakeDiscovery implements VlcDiscovery {

    private final List<String> calls;

    private boolean found;
    private @Nullable Path discoveredPath;
    private @Nullable Path loadedDirectory;

    FakeDiscovery(final List<String> calls) {
      this.calls = calls;
    }

    @Override
    public boolean discoverSystemInstallation() {
      this.calls.add(SYSTEM);
      return this.found;
    }

    @Override
    public @Nullable Path getDiscoveredPath() {
      return this.discoveredPath;
    }

    @Override
    public boolean loadInstalledLibraries(final Path libraryDirectory) {
      this.calls.add(LOAD);
      this.loadedDirectory = libraryDirectory;
      return this.found;
    }
  }
}
