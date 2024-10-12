/**
 * MIT License
 * <p>
 * Copyright (c) 2023 Brandon Li
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package rewrite.installers.vlc.search;

import static uk.co.caprica.vlcj.binding.lib.LibVlc.libvlc_new;
import static uk.co.caprica.vlcj.binding.lib.LibVlc.libvlc_release;

import com.sun.jna.NativeLibrary;
import com.sun.jna.StringArray;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import rewrite.installers.vlc.search.strategy.*;
import uk.co.caprica.vlcj.binding.internal.libvlc_instance_t;
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;

/**
 * Enhanced native discovery class for VLC. This class should not be touched by the user, and only
 * be used for internal library purposes.
 */
public class EnhancedNativeDiscovery {

  private static final List<NativeDiscoveryStrategy> DEFAULT_STRATEGIES = List.of(
          new LinuxNativeDiscoveryStrategy(),
          new OsxNativeDiscoveryStrategy(),
          new WindowsNativeDiscoveryStrategy()
  );

  private static boolean FOUND;

  private final List<NativeDiscoveryStrategy> discoveryStrategies;
  private NativeDiscoveryStrategy successfulStrategy;
  private String discoveredPath;

  public EnhancedNativeDiscovery(final NativeDiscoveryStrategy... discoveryStrategies) {
    this.discoveryStrategies =
            discoveryStrategies.length > 0 ? Arrays.asList(discoveryStrategies) : DEFAULT_STRATEGIES;
  }

  /**
   * Attempts to discover VLC on the host environment.
   *
   * @return whether discovery was successful or not
   */
  public final boolean discover() {
    if (FOUND) {
      return true;
    } else {
      for (final NativeDiscoveryStrategy discoveryStrategy : this.discoveryStrategies) {
        if (!discoveryStrategy.supported()) {
          continue;
        }
        final Optional<String> optional = discoveryStrategy.discover();
        if (optional.isPresent()) {
          final String path = optional.get();
          return this.attemptPath(discoveryStrategy, path);
        }
      }
      return false;
    }
  }

  private boolean attemptPath(final NativeDiscoveryStrategy discoveryStrategy, final String path) {
    this.addLibvlc(discoveryStrategy, path);
    this.tryPluginPath(path, discoveryStrategy);
    return this.attemptLibraryLoad(discoveryStrategy, path);
  }

  private boolean attemptLibraryLoad(
          final NativeDiscoveryStrategy discoveryStrategy, final String path) {
    if (this.tryLoadingLibrary()) {
      this.successfulStrategy = discoveryStrategy;
      this.discoveredPath = path;
      FOUND = true;
      return true;
    } else {
      return false;
    }
  }

  private void addLibvlc(final NativeDiscoveryStrategy discoveryStrategy, final String path) {
    if (discoveryStrategy.onFound(path)) {
      final String name = RuntimeUtil.getLibVlcLibraryName();
      NativeLibrary.addSearchPath(name, path);
    }
  }

  private void tryPluginPath(final String path, final NativeDiscoveryStrategy discoveryStrategy) {
    final String env = System.getenv(BaseNativeDiscoveryStrategy.PLUGIN_ENV_NAME);
    if (env == null || env.isEmpty()) {
      discoveryStrategy.onSetPluginPath(path);
    }
  }

  private boolean tryLoadingLibrary() {
    try {
      final String[] arguments = {"--reset-plugins-cache"};
      final libvlc_instance_t instance = libvlc_new(0, new StringArray(arguments));
      if (this.attemptNativeLibraryRelease(instance)) {
        return true;
      }
    } catch (final UnsatisfiedLinkError e) {
      throw new AssertionError(e);
    }
    return false;
  }

  private boolean attemptNativeLibraryRelease(final libvlc_instance_t instance) {
    if (instance != null) {
      libvlc_release(instance);
      final LibVlcVersion version = new LibVlcVersion();
      return version.isSupported();
    }
    return false;
  }

  public NativeDiscoveryStrategy getDiscoveryStrategy() {
    return this.successfulStrategy;
  }

  public String getDiscoveredPath() {
    return this.discoveredPath;
  }
}
