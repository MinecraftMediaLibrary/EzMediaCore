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
package rewrite.natives.strategy.implementation;

import rewrite.util.NativeUtils;

import java.nio.file.Path;

/**
 * Defines a local file system resource native library. It fetches from the user file system and
 * loads the proper library accordingly.
 */
public final class FileSystemResourceNativeLibrary extends NativeResourceLocator {

  public FileSystemResourceNativeLibrary(final String resource) {
    super(resource);
  }

  @Override
  public void loadIntoClassloader() {
    final String resource = this.getResource();
    try {
      System.load(resource);
      final Path path = Path.of(resource);
      final Path parent = path.getParent();
      NativeUtils.addSearchPath(parent);
    } catch (final Exception e) {
      throw new AssertionError(e);
    }
  }
}
