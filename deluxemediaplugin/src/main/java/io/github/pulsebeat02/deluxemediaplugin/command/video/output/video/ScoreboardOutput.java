/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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

import static rewrite.pipeline.output.DelayConfiguration.DELAY_20_MS;
import static rewrite.pipeline.output.Identifier.ofIdentifier;
import static rewrite.pipeline.output.Viewers.ofPlayers;
import static rewrite.dimension.Dimension.ofDimension;
import static io.github.pulsebeat02.ezmediacore.player.SoundKey.ofSound;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.player.VideoBuilder;
import java.util.Collection;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ScoreboardOutput extends VideoOutput {

  public ScoreboardOutput() {
    super("SCOREBOARD");
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

    final EzMediaCore core = plugin.library();
    final ScoreboardCallback.Builder builder = this.createScoreboardBuilder(attributes, players);

    final VideoBuilder videoBuilder = this.getBuilder(attributes);
    videoBuilder.dims(
        ofDimension(attributes.getResolutionWidth(), attributes.getResolutionHeight()));
    videoBuilder.video(builder.build(plugin.library()));
    videoBuilder.audio(attributes.getAudioOutput().build(core));

    return videoBuilder;
  }

  @NotNull
  private ScoreboardCallback.Builder createScoreboardBuilder(
      @NotNull final ScreenConfig attributes, @NotNull final Collection<? extends Player> players) {

    final Builder builder = VideoCallbackBuilder.scoreboard();

    builder.id(ofIdentifier(1080));
    builder.viewers(ofPlayers(players));
    builder.dims(ofDimension(attributes.getResolutionWidth(), attributes.getResolutionHeight()));
    builder.delay(DELAY_20_MS);

    return builder;
  }
}
