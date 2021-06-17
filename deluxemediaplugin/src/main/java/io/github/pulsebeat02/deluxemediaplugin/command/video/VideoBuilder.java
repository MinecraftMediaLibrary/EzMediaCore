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
import io.github.pulsebeat02.minecraftmedialibrary.frame.VideoPlayerContext;
import io.github.pulsebeat02.minecraftmedialibrary.frame.chat.ChatCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.chat.VLCChatPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.entity.EntityCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.entity.VLCEntityPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.entity.ScreenEntityType;
import io.github.pulsebeat02.minecraftmedialibrary.frame.highlight.BlockHighlightCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.highlight.VLCBlockHighlightPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.MapDataCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.VLCPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.scoreboard.ScoreboardCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.scoreboard.VLCScoreboardPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class VideoBuilder {

  private final MediaLibrary library;
  private final VideoCommandAttributes attributes;
  private final boolean linux;

  public VideoBuilder(
      @NotNull final MediaLibrary library, @NotNull final VideoCommandAttributes attributes) {
    this.library = library;
    this.attributes = attributes;
    linux = RuntimeUtilities.isLinux();
  }

  public VideoPlayerContext createMapPlayer() {
    return wrapCompatibility(
        VLCPlayer.builder()
            .url(attributes.getVideo().toString())
            .width(attributes.getScreenWidth())
            .height(attributes.getScreenHeight())
            .callback(
                MapDataCallback.builder()
                    .viewers(null)
                    .map(attributes.getStartingMap())
                    .itemframeWidth(attributes.getFrameWidth())
                    .itemframeHeight(attributes.getFrameHeight())
                    .videoWidth(attributes.getScreenWidth())
                    .delay(0)
                    .ditherHolder(attributes.getDither())
                    .build(library))
            .build(library));
  }

  public VideoPlayerContext createEntityPlayer(@NotNull final Player sender) {
    return wrapCompatibility(
        VLCEntityPlayer.builder()
            .url(attributes.getVideo().toString())
            .width(attributes.getScreenWidth())
            .height(attributes.getScreenHeight())
            .callback(
                EntityCallback.builder()
                    .viewers(null)
                    .entityWidth(attributes.getScreenWidth())
                    .entityHeight(attributes.getScreenHeight())
                    .delay(40)
                    .location(sender.getLocation())
                    .type(ScreenEntityType.ARMORSTAND)
                    .build(library))
            .build(library));
  }

  public VideoPlayerContext createChatBoxPlayer() {
    return wrapCompatibility(
        VLCChatPlayer.builder()
            .url(attributes.getVideo().toString())
            .width(attributes.getScreenWidth())
            .height(attributes.getScreenHeight())
            .callback(
                ChatCallback.builder()
                    .viewers(null)
                    .chatWidth(attributes.getScreenWidth())
                    .chatHeight(attributes.getScreenHeight())
                    .delay(40)
                    .build(library))
            .build(library));
  }

  public VideoPlayerContext createScoreboardPlayer() {
    return wrapCompatibility(
        VLCScoreboardPlayer.builder()
            .url(attributes.getVideo().toString())
            .width(attributes.getScreenWidth())
            .height(attributes.getScreenHeight())
            .callback(
                ScoreboardCallback.builder()
                    .viewers(null)
                    .scoreboardWidth(attributes.getScreenWidth())
                    .scoreboardHeight(attributes.getScreenHeight())
                    .delay(40)
                    .build(library))
            .build(library));
  }

  public VideoPlayerContext createBlockHighlightPlayer(@NotNull final Player sender) {
    return wrapCompatibility(
        VLCBlockHighlightPlayer.builder()
            .url(attributes.getVideo().toString())
            .width(attributes.getScreenWidth())
            .height(attributes.getScreenHeight())
            .callback(
                BlockHighlightCallback.builder()
                    .viewers(null)
                    .highlightWidth(attributes.getScreenWidth())
                    .highlightHeight(attributes.getScreenHeight())
                    .delay(40)
                    .location(sender.getLocation())
                    .build(library))
            .build(library));
  }

  public VideoPlayerContext wrapCompatibility(@NotNull final io.github.pulsebeat02.minecraftmedialibrary.frame.VLCPlayer context) {
    return linux ? context.toLinuxPlayer() : context;
  }
}
