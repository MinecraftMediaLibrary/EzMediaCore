/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.ffmpeg;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public interface FFmpegArgumentPreparation extends LibraryInjectable, EnhancedExecution {

  @NotNull
  FFmpegArgumentPreparation addArgument(@NotNull final String arg);

  @NotNull
  FFmpegArgumentPreparation addArguments(@NotNull final String key, @NotNull final String value);

  @NotNull
  FFmpegArgumentPreparation addArgument(@NotNull final String arg, final int index);

  @NotNull
  FFmpegArgumentPreparation addArguments(
      @NotNull final String key, @NotNull final String value, final int index);

  @NotNull
  FFmpegArgumentPreparation removeArgument(@NotNull final String arg);

  @NotNull
  FFmpegArgumentPreparation removeArgument(final int index);

  @NotNull
  FFmpegArgumentPreparation addMultipleArguments(@NotNull final String[] arguments);

  @NotNull
  FFmpegArgumentPreparation addMultipleArguments(@NotNull final Collection<String> arguments);

  void clearArguments();

  void onBeforeExecution();

  void onAfterExecution();

  boolean isCompleted();
}
