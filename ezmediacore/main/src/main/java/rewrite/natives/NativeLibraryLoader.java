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
package rewrite.natives;

import rewrite.util.os.Arch;
import rewrite.util.os.Bits;
import rewrite.util.os.OS;
import rewrite.util.os.Platform;
import rewrite.natives.strategy.implementation.ResourceLocator;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Main class for constructing a native library loader from the specified binaries and loading them
 * into the runtime.
 */
public final class NativeLibraryLoader {

  private final List<Entry<Platform, List<ResourceLocator>>> libs;

  NativeLibraryLoader(final List<Entry<Platform, List<ResourceLocator>>> libs) {
    this.libs = libs;
  }

  /**
   * Loads the native libraries.
   *
   * @param parallel whether the operation should be parallel over many native libraries
   *     (dependencies are still loaded before the specified binary however).
   */
  public void load(final boolean parallel) {
    final Stream<Entry<Platform, List<ResourceLocator>>> stream = this.libs.stream();
    this.applyOperations(parallel ? stream.parallel() : stream);
  }

  private void applyOperations(final Stream<Entry<Platform, List<ResourceLocator>>> stream) {
    stream.filter(testOperatingSystem())
        .forEach(loadIntoClassloader());
  }

  private static Consumer<Entry<Platform, List<ResourceLocator>>> loadIntoClassloader() {
    return entry -> {
      final List<ResourceLocator> locators = entry.getValue();
      locators.forEach(ResourceLocator::loadIntoClassloader);
    };
  }

  private static Predicate<Entry<Platform, List<ResourceLocator>>> testOperatingSystem() {
    return entry -> {
      final Platform platform = entry.getKey();
      return platform.matchesCurrentOS();
    };
  }

  /**
   * Create a new builder for NativeLibraryLoader.
   *
   * @return creates a builder for NativeLibraryLoader
   */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final List<Entry<Platform, List<ResourceLocator>>> libs;

    {
      this.libs = new ArrayList<>();
    }

    private Builder() {}

    /**
     * Adds a native library to be loaded at runtime.
     *
     * @param os the operating system
     * @param arch the architecture
     * @param bits the bits
     * @param location the location of the binary
     * @param dependencies native dependencies to be loaded first. Dependencies should be ordered
     *     correctly based on how they should load (order matters here!).
     * @return the same builder
     */
    public Builder addNativeLibrary(
        final OS os,
        final Arch arch,
        final Bits bits,
        final ResourceLocator location,
        final ResourceLocator... dependencies) {

      final List<ResourceLocator> unmodifiable = List.of(dependencies);
      final List<ResourceLocator> locators = new ArrayList<>(unmodifiable);
      locators.add(location);

      final Platform platform = Platform.ofPlatform(os, arch, bits);
      final SimpleImmutableEntry<Platform, List<ResourceLocator>> entry = new SimpleImmutableEntry<>(platform, locators);
      this.libs.add(entry);

      return this;
    }

    /**
     * Adds a native library to be loaded at runtime.
     *
     * @param platform the platform the binary is targeted on
     * @param location the location of the binary
     * @param dependencies native dependencies to be loaded first. Dependencies should be ordered *
     *     correctly based on how they should load (order matters here!).
     * @return the same builder
     */
    public Builder addNativeLibrary(
        final Platform platform,
        final ResourceLocator location,
        final ResourceLocator... dependencies) {
      final OS os = platform.getOS();
      final Arch arch = platform.getArch();
      final Bits bits = platform.getBits();
      return this.addNativeLibrary(os, arch, bits, location, dependencies);
    }

    /**
     * Builds a new NativeLibraryLoader for usage.
     *
     * @return a new NativeLibraryLoader
     */
    public NativeLibraryLoader build() {
      return new NativeLibraryLoader(this.libs);
    }
  }
}
