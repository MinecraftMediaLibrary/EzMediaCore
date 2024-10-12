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
package rewrite.installers.vlc.search.provider;

import rewrite.installers.vlc.search.provider.linux.LinuxWellKnownDirectoryProvider;
import rewrite.installers.vlc.search.provider.misc.ConfigurationFileDiscoveryDirectoryProvider;
import rewrite.installers.vlc.search.provider.misc.CustomWellKnownDirectoryProvider;
import rewrite.installers.vlc.search.provider.misc.JnaLibraryPathDirectoryProvider;
import rewrite.installers.vlc.search.provider.osx.OsxWellKnownDirectoryProvider;
import rewrite.installers.vlc.search.provider.path.SystemPathDirectoryProvider;
import rewrite.installers.vlc.search.provider.path.UserDirDirectoryProvider;
import rewrite.installers.vlc.search.provider.win.WindowsInstallDirectoryProvider;
import rewrite.installers.vlc.search.strategy.BaseNativeDiscoveryStrategy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class DirectoryProviderDiscoveryStrategy extends BaseNativeDiscoveryStrategy {

  private static final List<DiscoveryDirectoryProvider> DIRECTORY_PROVIDERS;

  static {
    DIRECTORY_PROVIDERS = new ArrayList<>();
    DIRECTORY_PROVIDERS.add(new ConfigurationFileDiscoveryDirectoryProvider());
    DIRECTORY_PROVIDERS.add(new JnaLibraryPathDirectoryProvider());
    DIRECTORY_PROVIDERS.add(new LinuxWellKnownDirectoryProvider());
    DIRECTORY_PROVIDERS.add(new OsxWellKnownDirectoryProvider());
    DIRECTORY_PROVIDERS.add(new SystemPathDirectoryProvider());
    DIRECTORY_PROVIDERS.add(new UserDirDirectoryProvider());
    DIRECTORY_PROVIDERS.add(new WindowsInstallDirectoryProvider());
    DIRECTORY_PROVIDERS.sort(getDiscoveryDirectoryProviderComparator());
  }

  private static Comparator<DiscoveryDirectoryProvider> getDiscoveryDirectoryProviderComparator() {
    return (o1, o2) -> o2.priority() - o1.priority();
  }

  private final List<DiscoveryDirectoryProvider> directoryProviders;

  public DirectoryProviderDiscoveryStrategy(
      final String[] filenamePatterns, final String[] pluginPathFormats) {
    super(filenamePatterns, pluginPathFormats);
    this.directoryProviders = DIRECTORY_PROVIDERS;
  }

  @Override
  public final List<String> discoveryDirectories() {
    final List<String> directories = new ArrayList<>();
    for (final DiscoveryDirectoryProvider provider : this.getSupportedProviders()) {
      final String[] paths = provider.directories();
      final List<String> list = Arrays.asList(paths);
      directories.addAll(list);
    }
    return directories;
  }

  private List<DiscoveryDirectoryProvider> getSupportedProviders() {
    final List<DiscoveryDirectoryProvider> result = new ArrayList<>();
    for (final DiscoveryDirectoryProvider list : this.directoryProviders) {
      if (list.supported()) {
        result.add(list);
      }
    }
    return this.sort(result);
  }

  private List<DiscoveryDirectoryProvider> sort(final List<DiscoveryDirectoryProvider> providers) {
    return providers;
  }

  public static void addSearchDirectory(final Path path) {
    final String raw = path.toString();
    final CustomWellKnownDirectoryProvider provider = new CustomWellKnownDirectoryProvider(raw);
    DIRECTORY_PROVIDERS.add(provider);
    DIRECTORY_PROVIDERS.sort(getDiscoveryDirectoryProviderComparator());
  }
}
