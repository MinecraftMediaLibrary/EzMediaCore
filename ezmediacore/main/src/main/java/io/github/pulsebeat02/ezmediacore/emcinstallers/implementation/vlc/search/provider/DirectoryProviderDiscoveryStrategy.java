/**
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
package io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.search.provider;

import io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.DiscoveryDirectoryProvider;
import io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.linux.LinuxWellKnownDirectoryProvider;
import io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.misc.ConfigurationFileDiscoveryDirectoryProvider;
import io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.misc.CustomWellKnownDirectoryProvider;
import io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.misc.JnaLibraryPathDirectoryProvider;
import io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.osx.OsxWellKnownDirectoryProvider;
import io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.path.SystemPathDirectoryProvider;
import io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.path.UserDirDirectoryProvider;
import io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.win.WindowsInstallDirectoryProvider;
import io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.search.strategy.BaseNativeDiscoveryStrategy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class DirectoryProviderDiscoveryStrategy extends BaseNativeDiscoveryStrategy {

  private static final List<io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.DiscoveryDirectoryProvider> DIRECTORY_PROVIDERS;

  static {
    DIRECTORY_PROVIDERS =
        new ArrayList<>(
            Arrays.asList(
                new ConfigurationFileDiscoveryDirectoryProvider(),
                new JnaLibraryPathDirectoryProvider(),
                new LinuxWellKnownDirectoryProvider(),
                new OsxWellKnownDirectoryProvider(),
                new SystemPathDirectoryProvider(),
                new UserDirDirectoryProvider(),
                new WindowsInstallDirectoryProvider()));
    DIRECTORY_PROVIDERS.sort((o1, o2) -> o2.priority() - o1.priority());
  }

  private final List<io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.DiscoveryDirectoryProvider> directoryProviders;

  public DirectoryProviderDiscoveryStrategy(
      final String[] filenamePatterns, final String[] pluginPathFormats) {
    super(filenamePatterns, pluginPathFormats);
    this.directoryProviders = DIRECTORY_PROVIDERS;
  }

  @Override
  public final List<String> discoveryDirectories() {
    final List<String> directories = new ArrayList<>();
    for (final io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.DiscoveryDirectoryProvider provider : this.getSupportedProviders()) {
      directories.addAll(Arrays.asList(provider.directories()));
    }
    return directories;
  }

  private List<io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.DiscoveryDirectoryProvider> getSupportedProviders() {
    final List<io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.DiscoveryDirectoryProvider> result = new ArrayList<>();
    for (final io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.DiscoveryDirectoryProvider list : this.directoryProviders) {
      if (list.supported()) {
        result.add(list);
      }
    }
    return this.sort(result);
  }

  private List<io.github.pulsebeat02.emcinstallers.implementation.vlc.search.provider.DiscoveryDirectoryProvider> sort(final List<DiscoveryDirectoryProvider> providers) {
    return providers;
  }

  public static void addSearchDirectory(final Path path) {
    DIRECTORY_PROVIDERS.add(new CustomWellKnownDirectoryProvider(path.toString()));
    DIRECTORY_PROVIDERS.sort((o1, o2) -> o2.priority() - o1.priority());
  }
}
