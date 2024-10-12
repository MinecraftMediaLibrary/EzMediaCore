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
package io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.search.provider.misc;

import io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.search.provider.DiscoveryDirectoryProvider;
import io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.search.provider.DiscoveryProviderPriority;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigurationFileDiscoveryDirectoryProvider implements DiscoveryDirectoryProvider {

  private static final String CONFIG_DIR;
  private static final String CONFIG_FILE_NAME;
  private static final String PROPERTY_NAME;

  static {
    CONFIG_DIR = String.format("%s/.config/vlcj", System.getProperty("user.home"));
    CONFIG_FILE_NAME = "vlcj.config";
    PROPERTY_NAME = "nativeDirectory";
  }

  @Override
  public int priority() {
    return DiscoveryProviderPriority.CONFIG_FILE;
  }

  @Override
  public String[] directories() {
    final Path file = Paths.get(CONFIG_DIR, CONFIG_FILE_NAME);
    final Properties properties = new Properties();
    try (final Reader reader = Files.newBufferedReader(file)) {
      properties.load(reader);
      final String directory = properties.getProperty(PROPERTY_NAME);
      if (directory != null) {
        return new String[]{directory};
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return new String[0];
  }

  @Override
  public boolean supported() {
    final Path file = Paths.get(CONFIG_DIR, CONFIG_FILE_NAME);
    return Files.exists(file) && Files.isRegularFile(file) && Files.isReadable(file);
  }
}
