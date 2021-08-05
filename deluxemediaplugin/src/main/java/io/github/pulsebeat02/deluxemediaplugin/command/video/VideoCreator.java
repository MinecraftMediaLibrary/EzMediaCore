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
import io.github.pulsebeat02.ezmediacore.callback.BlockHighlightCallback;
import io.github.pulsebeat02.ezmediacore.callback.ChatCallback;
import io.github.pulsebeat02.ezmediacore.callback.EntityCallback;
import io.github.pulsebeat02.ezmediacore.callback.EntityType;
import io.github.pulsebeat02.ezmediacore.callback.MapCallback;
import io.github.pulsebeat02.ezmediacore.callback.ScoreboardCallback;
import io.github.pulsebeat02.ezmediacore.callback.entity.NamedEntityString;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.VideoFactory;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import java.util.Collection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record VideoCreator(MediaLibraryCore library,
                           VideoCommandAttributes attributes) {

  public VideoCreator(
      @NotNull final MediaLibraryCore library, @NotNull final VideoCommandAttributes attributes) {
    this.library = library;
    this.attributes = attributes;
  }

  public VideoPlayer createMapPlayer(@NotNull final Collection<? extends Player> viewers) {
    return VideoFactory.builder()
        .url(this.attributes.getMrl())
        .callback(
            new MapCallback(
                this.library,
                Dimension.of(
                    this.attributes.getPixelWidth(), this.attributes.getPixelHeight()),
                viewers,
                this.attributes.getDither().getAlgorithm(),
                this.attributes.getMap(),
                this.attributes.getFrameWidth(),
                0))
        .build();
  }

  public VideoPlayer createEntityPlayer(
      @NotNull final Player sender, @NotNull final Collection<? extends Player> viewers) {
    return VideoFactory.builder()
        .url(this.attributes.getMrl())
        .callback(
            new EntityCallback(
                this.library,
                Dimension.of(
                    this.attributes.getPixelWidth(), this.attributes.getPixelHeight()),
                viewers,
                sender.getLocation(),
                NamedEntityString.NORMAL_SQUARE,
                EntityType.ARMORSTAND,
                this.attributes.getFrameWidth(),
                20))
        .build();
  }

  public VideoPlayer createChatBoxPlayer(@NotNull final Collection<? extends Player> viewers) {
    return VideoFactory.builder()
        .url(this.attributes.getMrl())
        .callback(
            new ChatCallback(
                this.library,
                Dimension.of(
                    this.attributes.getPixelWidth(), this.attributes.getPixelHeight()),
                viewers,
                NamedEntityString.NORMAL_SQUARE,
                this.attributes.getFrameWidth(),
                20))
        .build();
  }

  public VideoPlayer createScoreboardPlayer(@NotNull final Collection<? extends Player> viewers) {
    return VideoFactory.builder()
        .url(this.attributes.getMrl())
        .callback(
            new ScoreboardCallback(
                this.library,
                Dimension.of(
                    this.attributes.getPixelWidth(), this.attributes.getPixelHeight()),
                viewers,
                1080,
                this.attributes.getFrameWidth(),
                20))
        .build();
  }

  public VideoPlayer createBlockHighlightPlayer(
      @NotNull final Player sender, @NotNull final Collection<? extends Player> viewers) {
    return VideoFactory.builder()
        .url(this.attributes.getMrl())
        .callback(
            new BlockHighlightCallback(
                this.library,
                Dimension.of(
                    this.attributes.getPixelWidth(), this.attributes.getPixelHeight()),
                viewers,
                sender.getLocation(),
                this.attributes.getFrameWidth(),
                40))
        .build();
  }
}
