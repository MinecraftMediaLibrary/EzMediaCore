/*.........................................................................................
. Copyright © 2021 Brandon Li
.                                                                                        .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this
. software and associated documentation files (the “Software”), to deal in the Software
. without restriction, including without limitation the rights to use, copy, modify, merge,
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit
. persons to whom the Software is furnished to do so, subject to the following conditions:
.
. The above copyright notice and this permission notice shall be included in all copies
. or substantial portions of the Software.
.
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
. EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
. MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
. NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
. BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
. ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
. CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
.  SOFTWARE.
.                                                                                        .
.........................................................................................*/

package io.github.pulsebeat02.deluxemediaplugin.command.video;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.VideoPlayerContext;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.VideoPlayerProvider;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.callback.BlockHighlightCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.callback.ChatCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.callback.EntityCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.callback.MapDataCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.callback.ScoreboardCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.entity.ScreenEntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class VideoBuilder {

  private final MediaLibrary library;
  private final VideoCommandAttributes attributes;

  public VideoBuilder(
      @NotNull final MediaLibrary library, @NotNull final VideoCommandAttributes attributes) {
    this.library = library;
    this.attributes = attributes;
  }

  public VideoPlayerContext createMapPlayer() {
    return VideoPlayerProvider.createMapPlayer(
        library,
        attributes.getVideo().toString(),
        MapDataCallback.builder()
            .viewers(null)
            .map(attributes.getStartingMap())
            .itemframeWidth(attributes.getFrameWidth())
            .itemframeHeight(attributes.getFrameHeight())
            .videoWidth(attributes.getScreenWidth())
            .delay(0)
            .ditherHolder(attributes.getDither())
            .build(library),
        attributes.getScreenWidth(),
        attributes.getScreenHeight());
  }

  public VideoPlayerContext createEntityPlayer(@NotNull final Player sender) {
    return VideoPlayerProvider.createEntityPlayer(
        library,
        attributes.getVideo().toString(),
        EntityCallback.builder()
            .viewers(null)
            .entityWidth(attributes.getScreenWidth())
            .entityHeight(attributes.getScreenHeight())
            .delay(40)
            .location(sender.getLocation())
            .type(ScreenEntityType.ARMORSTAND)
            .build(library),
        attributes.getScreenWidth(),
        attributes.getScreenHeight());
  }

  public VideoPlayerContext createChatBoxPlayer() {
    return VideoPlayerProvider.createChatPlayer(
        library,
        attributes.getVideo().toString(),
        ChatCallback.builder()
            .viewers(null)
            .chatWidth(attributes.getScreenWidth())
            .chatHeight(attributes.getScreenHeight())
            .delay(40)
            .build(library),
        attributes.getScreenWidth(),
        attributes.getScreenHeight());
  }

  public VideoPlayerContext createScoreboardPlayer() {
    return VideoPlayerProvider.createScoreboardPlayer(
        library,
        attributes.getVideo().toString(),
        ScoreboardCallback.builder()
            .viewers(null)
            .scoreboardWidth(attributes.getScreenWidth())
            .scoreboardHeight(attributes.getScreenHeight())
            .delay(40)
            .build(library),
        attributes.getScreenWidth(),
        attributes.getScreenHeight());
  }

  public VideoPlayerContext createBlockHighlightPlayer(@NotNull final Player sender) {
    return VideoPlayerProvider.createBlockHighlightPlayer(
        library,
        attributes.getVideo().toString(),
        BlockHighlightCallback.builder()
            .viewers(null)
            .highlightWidth(attributes.getScreenWidth())
            .highlightHeight(attributes.getScreenHeight())
            .delay(40)
            .location(sender.getLocation())
            .build(library),
        attributes.getScreenWidth(),
        attributes.getScreenHeight());
  }
}
