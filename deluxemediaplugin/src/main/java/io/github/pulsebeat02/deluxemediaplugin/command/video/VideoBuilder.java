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

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
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

  private final MediaLibraryCore library;
  private final VideoCommandAttributes attributes;

  public VideoBuilder(
      @NotNull final MediaLibraryCore library, @NotNull final VideoCommandAttributes attributes) {
    this.library = library;
    this.attributes = attributes;
  }



  public VideoPlayer createMapPlayer() {
    return VideoBuilder.builder()



    return VideoBuilder.createMapPlayer(
            this.library,
            this.attributes.getVideo().toString(),
        MapDataCallback.builder()
            .viewers(null)
            .map(this.attributes.getStartingMap())
            .itemframeWidth(this.attributes.getFrameWidth())
            .itemframeHeight(this.attributes.getFrameHeight())
            .videoWidth(this.attributes.getScreenWidth())
            .delay(0)
            .ditherHolder(this.attributes.getDither())
            .build(this.library),
            this.attributes.getScreenWidth(),
            this.attributes.getScreenHeight());
  }

  public VideoPlayer createEntityPlayer(@NotNull final Player sender) {
    return VideoPlayerProvider.createEntityPlayer(
            this.library,
            this.attributes.getVideo().toString(),
        EntityCallback.builder()
            .viewers(null)
            .entityWidth(this.attributes.getScreenWidth())
            .entityHeight(this.attributes.getScreenHeight())
            .delay(40)
            .location(sender.getLocation())
            .type(ScreenEntityType.ARMORSTAND)
            .build(this.library),
            this.attributes.getScreenWidth(),
            this.attributes.getScreenHeight());
  }

  public VideoPlayer createChatBoxPlayer() {
    return VideoPlayerProvider.createChatPlayer(
            this.library,
            this.attributes.getVideo().toString(),
        ChatCallback.builder()
            .viewers(null)
            .chatWidth(this.attributes.getScreenWidth())
            .chatHeight(this.attributes.getScreenHeight())
            .delay(40)
            .build(this.library),
            this.attributes.getScreenWidth(),
            this.attributes.getScreenHeight());
  }

  public VideoPlayer createScoreboardPlayer() {
    return VideoPlayerProvider.createScoreboardPlayer(
            this.library,
            this.attributes.getVideo().toString(),
        ScoreboardCallback.builder()
            .viewers(null)
            .scoreboardWidth(this.attributes.getScreenWidth())
            .scoreboardHeight(this.attributes.getScreenHeight())
            .delay(40)
            .build(this.library),
            this.attributes.getScreenWidth(),
            this.attributes.getScreenHeight());
  }

  public VideoPlayer createBlockHighlightPlayer(@NotNull final Player sender) {
    return VideoPlayerProvider.createBlockHighlightPlayer(
            this.library,
            this.attributes.getVideo().toString(),
        BlockHighlightCallback.builder()
            .viewers(null)
            .highlightWidth(this.attributes.getScreenWidth())
            .highlightHeight(this.attributes.getScreenHeight())
            .delay(40)
            .location(sender.getLocation())
            .build(this.library),
            this.attributes.getScreenWidth(),
            this.attributes.getScreenHeight());
  }
}
