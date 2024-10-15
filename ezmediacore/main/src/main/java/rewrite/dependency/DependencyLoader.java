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
package rewrite.dependency;

import rewrite.EzMediaCore;
import rewrite.capabilities.Capabilities;
import rewrite.capabilities.Capability;
import rewrite.capabilities.SelectCapability;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DependencyLoader {

  private final EzMediaCore core;
  private final SelectCapability[] capabilities;

  public DependencyLoader(final EzMediaCore core, final SelectCapability[] capabilities) {
    this.core = core;
    this.capabilities = capabilities;
  }

  public void start() {
    final Set<CompletableFuture<Void>> tasks = new HashSet<>();
    for (final SelectCapability capability : this.capabilities) {
      final CompletableFuture<Void> install = CompletableFuture.runAsync(() -> this.getTrueCapability(capability));
      tasks.add(install);
    }
    tasks.add(CompletableFuture.runAsync(this::installNativeLibraries));
    CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();
  }

  public void getTrueCapability(final SelectCapability capability) {
    final Capability switchCapability = switch (capability) {
      case VLC -> Capabilities.VLC;
      case FFMPEG -> Capabilities.FFMPEG;
      case RTSP -> Capabilities.RTSP;
      case YT_DLP -> Capabilities.YT_DLP;
    };
    switchCapability.isEnabled();
  }

  private void installNativeLibraries() {
    final DitherDependencyManager dither = new DitherDependencyManager(this.core);
    dither.start();
  }

  public EzMediaCore getCore() {
    return this.core;
  }
}
