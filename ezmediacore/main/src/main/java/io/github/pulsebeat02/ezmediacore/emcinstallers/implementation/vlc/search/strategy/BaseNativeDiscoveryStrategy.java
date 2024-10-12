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
package io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.search.strategy;

import io.github.pulsebeat02.emcinstallers.implementation.vlc.search.strategy.NativeDiscoveryStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class BaseNativeDiscoveryStrategy implements NativeDiscoveryStrategy {

  public static final String PLUGIN_ENV_NAME = "VLC_PLUGIN_PATH";

  private final Pattern[] patternsToMatch;
  private final String[] pluginPathFormats;

  public BaseNativeDiscoveryStrategy(final String[] filenamePatterns,
      final String[] pluginPathFormats) {
    this.patternsToMatch = new Pattern[filenamePatterns.length];
    this.pluginPathFormats = pluginPathFormats;
    this.compilePatterns(filenamePatterns);
  }

  private void compilePatterns(final String[] filenamePatterns) {
    for (int i = 0; i < filenamePatterns.length; i++) {
      this.patternsToMatch[i] = Pattern.compile(filenamePatterns[i]);
    }
  }

  @Override
  public final Optional<String> discover() {
    return this.discoveryDirectories().stream().parallel().filter(this::find).findAny();
  }

  protected abstract List<String> discoveryDirectories();

  private boolean find(final String directoryName) {

    final Path dir = Paths.get(directoryName);
    if (Files.notExists(dir)) {
      return false;
    }

    final Set<String> matches = new HashSet<>(this.patternsToMatch.length);
    try (final Stream<Path> stream = Files.walk(dir, 1).parallel()) {
      return stream.anyMatch(this.getMatchingPatternFile(matches));
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return false;
  }

  private Predicate<Path> getMatchingPatternFile(final Set<String> matches) {
    return file -> {
      for (final Pattern pattern : this.patternsToMatch) {
        if (this.validatePattern(matches, file, pattern)) {
          return true;
        }
      }
      return false;
    };
  }

  private boolean validatePattern(final Set<String> matches, final Path file,
      final Pattern pattern) {
    final Matcher matcher = pattern.matcher(file.getFileName().toString());
    if (matcher.matches()) {
      matches.add(pattern.pattern());
      return matches.size() == this.patternsToMatch.length;
    }
    return false;
  }

  @Override
  public boolean onFound(final String path) {
    return true;
  }

  @Override
  public final boolean onSetPluginPath(final String path) {
    for (final String pathFormat : this.pluginPathFormats) {
      final String pluginPath = String.format(pathFormat, path);
      if (Files.exists(Paths.get(pluginPath))) {
        return this.setPluginPath(pluginPath);
      }
    }
    return false;
  }

  protected abstract boolean setPluginPath(String pluginPath);

}
