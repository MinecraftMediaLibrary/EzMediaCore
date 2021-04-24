/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary.concurrent;

import com.github.pulsebeat02.minecraftmedialibrary.extractor.VideoExtractor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/** An async helper class used for extracting videos.F */
public class AsyncVideoExtraction {

  private final VideoExtractor extractor;

  /**
   * Instantiates a new AsyncVideoExtraction.
   *
   * @param extractor the extractor
   */
  public AsyncVideoExtraction(@NotNull final VideoExtractor extractor) {
    this.extractor = extractor;
  }

  /**
   * Download a video using CompletableFuture.
   *
   * @return the completable future
   */
  @NotNull
  public CompletableFuture<Path> downloadVideo() {
    return CompletableFuture.supplyAsync(extractor::downloadVideo);
  }

  /**
   * Extract audio using CompletableFuture.
   *
   * @return the completable future
   */
  @NotNull
  public CompletableFuture<Path> extractAudio() {
    return CompletableFuture.supplyAsync(extractor::extractAudio);
  }
}
