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

package io.github.pulsebeat02.deluxemediaplugin.command.video.output;

import io.github.pulsebeat02.deluxemediaplugin.command.video.output.video.ChatOutput;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.video.DebugOutput;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.video.EntityOutput;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.video.ItemframeOutput;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.video.PlaybackOutputHandle;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.video.ScoreboardOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public enum VideoPlayback {
  ITEMFRAME(new ItemframeOutput()),
  ARMOR_STAND(new EntityOutput()),
  CHATBOX(new ChatOutput()),
  DEBUG_HIGHLIGHTS(new DebugOutput()),
  SCOREBOARD(new ScoreboardOutput());

  private static final Map<String, VideoPlayback> KEYS;

  static {
    KEYS = new HashMap<>();
    for (final VideoPlayback type : VideoPlayback.values()) {
      KEYS.put(type.handle.getName(), type);
    }
  }

  private final PlaybackOutputHandle handle;

  VideoPlayback(@NotNull final PlaybackOutputHandle handle) {
    this.handle = handle;
  }

  public static @NotNull Optional<VideoPlayback> ofKey(@NotNull final String str) {
    return Optional.ofNullable(KEYS.get(str));
  }

  public @NotNull PlaybackOutputHandle getHandle() {
    return this.handle;
  }
}
