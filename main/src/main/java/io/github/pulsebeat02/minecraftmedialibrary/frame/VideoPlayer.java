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

package io.github.pulsebeat02.minecraftmedialibrary.frame;

import com.google.common.collect.ImmutableMap;
import io.github.pulsebeat02.minecraftmedialibrary.frame.chat.ChatPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.chat.LinuxChatPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.entity.EntityPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.entity.LinuxEntityPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.highlight.BlockHighlightPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.highlight.LinuxBlockHighlightPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.LinuxMapPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.MapPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.scoreboard.LinuxScoreboardPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.scoreboard.ScoreboardPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class VideoPlayer {

  private static final Map<Class<?>, Class<?>> MAPPINGS;

  static {
    MAPPINGS =
        ImmutableMap.of(
            ChatPlayer.class, LinuxChatPlayer.class,
            EntityPlayer.class, LinuxEntityPlayer.class,
            BlockHighlightPlayer.class, LinuxBlockHighlightPlayer.class,
            MapPlayer.class, LinuxMapPlayer.class,
            ScoreboardPlayer.class, LinuxScoreboardPlayer.class);
  }

  public VideoPlayer(@NotNull final VideoPlayerContext player) {
    final Class<?> clazz = player.getClass();
  }
}
