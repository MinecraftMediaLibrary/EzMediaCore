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
package io.github.pulsebeat02.deluxemediaplugin.command.video.output.video;

import static io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration.DELAY_20_MS;
import static io.github.pulsebeat02.ezmediacore.callback.Identifier.ofIdentifier;
import static io.github.pulsebeat02.ezmediacore.callback.Viewers.ofPlayers;
import static io.github.pulsebeat02.ezmediacore.dimension.Dimension.ofDimension;
import static io.github.pulsebeat02.ezmediacore.player.SoundKey.ofSound;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.ezmediacore.callback.CallbackBuilder;
import io.github.pulsebeat02.ezmediacore.callback.ScoreboardCallback;
import io.github.pulsebeat02.ezmediacore.callback.ScoreboardCallback.Builder;
import io.github.pulsebeat02.ezmediacore.player.VideoBuilder;
import java.util.Collection;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DebugOutput extends VideoOutput {

  public DebugOutput() {
    super("DEBUG_HIGHLIGHTS");
  }

  @Override
  public boolean createVideoPlayer(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final ScreenConfig attributes,
      @NotNull final CommandSender sender,
      @NotNull final Collection<? extends Player> players) {
    attributes.setPlayer(this.createVideoBuilder(plugin, attributes, players).build());
    return true;
  }

  @NotNull
  private VideoBuilder createVideoBuilder(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final ScreenConfig attributes,
      @NotNull final Collection<? extends Player> players) {

    final ScoreboardCallback.Builder builder =
        this.createDebugHighlightsBuilder(attributes, players);

    final VideoBuilder videoBuilder = this.getBuilder(attributes);
    videoBuilder.soundKey(ofSound("emc"));
    videoBuilder.callback(builder.build(plugin.library()));

    return videoBuilder;
  }

  @NotNull
  private ScoreboardCallback.Builder createDebugHighlightsBuilder(
      @NotNull final ScreenConfig attributes, @NotNull final Collection<? extends Player> players) {

    final Builder builder = CallbackBuilder.scoreboard();

    builder.id(ofIdentifier(1080));
    builder.viewers(ofPlayers(players));
    builder.dims(ofDimension(attributes.getResolutionWidth(), attributes.getResolutionHeight()));
    builder.delay(DELAY_20_MS);

    return builder;
  }
}
