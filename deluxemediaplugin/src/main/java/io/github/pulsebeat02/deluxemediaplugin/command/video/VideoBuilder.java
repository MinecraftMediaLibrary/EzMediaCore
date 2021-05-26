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

package io.github.pulsebeat02.deluxemediaplugin.command.video;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.frame.chat.ChatCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.chat.ChatPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.entity.EntityCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.entity.EntityPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.entity.ScreenEntityType;
import io.github.pulsebeat02.minecraftmedialibrary.frame.highlight.BlockHighlightCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.highlight.BlockHighlightPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.MapDataCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.MapPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.scoreboard.ScoreboardCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.scoreboard.ScoreboardPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VideoBuilder {

  private final MediaLibrary library;
  private final MinecraftVideoAttributes attributes;

  public VideoBuilder(
      @NotNull final MediaLibrary library, @NotNull final MinecraftVideoAttributes attributes) {
    this.library = library;
    this.attributes = attributes;
  }

  public MapPlayer createMapPlayer() {
    return MapPlayer.builder()
        .setUrl(attributes.getFile().getAbsolutePath())
        .setWidth(attributes.getScreenWidth())
        .setHeight(attributes.getScreenHeight())
        .setCallback(
            MapDataCallback.builder()
                .setViewers(null)
                .setMap(attributes.getStartingMap())
                .setItemframeWidth(attributes.getFrameWidth())
                .setItemframeHeight(attributes.getFrameHeight())
                .setVideoWidth(attributes.getScreenWidth())
                .setDelay(0)
                .setDitherHolder(attributes.getDither())
                .build(library))
        .build(library);
  }

  public EntityPlayer createEntityCloudPlayer(@NotNull final Player sender) {
    return EntityPlayer.builder()
        .setUrl(attributes.getFile().getAbsolutePath())
        .setWidth(attributes.getScreenWidth())
        .setHeight(attributes.getScreenHeight())
        .setCallback(
            EntityCallback.builder()
                .setViewers(null)
                .setEntityWidth(attributes.getScreenWidth())
                .setEntityHeight(attributes.getScreenHeight())
                .setDelay(40)
                .setLocation(sender.getLocation())
                .setType(ScreenEntityType.AREA_EFFECT_CLOUD)
                .build(library))
        .build(library);
  }

  public ChatPlayer createChatBoxPlayer() {
    return ChatPlayer.builder()
        .setUrl(attributes.getFile().getAbsolutePath())
        .setWidth(attributes.getScreenWidth())
        .setHeight(attributes.getScreenHeight())
        .setCallback(
            ChatCallback.builder()
                .setViewers(null)
                .setChatWidth(attributes.getScreenWidth())
                .setChatHeight(attributes.getScreenHeight())
                .setDelay(40)
                .build(library))
        .build(library);
  }

  public ScoreboardPlayer createScoreboardPlayer() {
    return ScoreboardPlayer.builder()
        .setUrl(attributes.getFile().getAbsolutePath())
        .setWidth(attributes.getScreenWidth())
        .setHeight(attributes.getScreenHeight())
        .setCallback(
            ScoreboardCallback.builder()
                .setViewers(null)
                .setScoreboardWidth(attributes.getScreenWidth())
                .setScoreboardHeight(attributes.getScreenHeight())
                .setDelay(40)
                .build(library))
        .build(library);
  }

  public BlockHighlightPlayer createBlockHighlightPlayer(@NotNull final Player sender) {
    return BlockHighlightPlayer.builder()
        .setUrl(attributes.getFile().getAbsolutePath())
        .setWidth(attributes.getScreenWidth())
        .setHeight(attributes.getScreenHeight())
        .setCallback(
            BlockHighlightCallback.builder()
                .setViewers(null)
                .setHighlightWidth(attributes.getScreenWidth())
                .setHighlightHeight(attributes.getScreenHeight())
                .setDelay(40)
                .setLocation(sender.getLocation())
                .build(library))
        .build(library);
  }
}
