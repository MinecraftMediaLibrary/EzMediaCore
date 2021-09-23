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
package io.github.pulsebeat02.ezmediacore.utility;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.player.MrlConfiguration;
import io.github.pulsebeat02.ezmediacore.throwable.InvalidMRLException;
import java.io.IOException;
import java.util.List;
import org.jcodec.common.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ArgumentUtils {

  private ArgumentUtils() {}

  public static @NotNull MrlConfiguration retrievePlayerArguments(
      @NotNull final Object @Nullable [] arguments) {
    Preconditions.checkArgument(arguments != null, "Arguments cannot be null!");
    Preconditions.checkArgument(
        arguments.length > 0, "Invalid argument length! Must have at least 1!");
    final Object mrl = arguments[0];
    Preconditions.checkArgument(
        mrl instanceof MrlConfiguration, "Invalid MRL type! Must be a MrlConfiguration!");
    final MrlConfiguration configuration = (MrlConfiguration) mrl;
    final String url = configuration.getMrl();
    try {
      final List<String> list = RequestUtils.getVideoURLs(url);
      if (list.isEmpty()) {
        throw new InvalidMRLException(url);
      }
      final String use = list.get(0);
      Logger.info("Extracted URL (youtube-dl): %s".formatted(use));
      return MrlConfiguration.ofMrl(use);
    } catch (final IOException | InterruptedException ignored) {
    }
    Logger.info("Extracted URL: %s".formatted(url));
    return configuration;
  }
}
