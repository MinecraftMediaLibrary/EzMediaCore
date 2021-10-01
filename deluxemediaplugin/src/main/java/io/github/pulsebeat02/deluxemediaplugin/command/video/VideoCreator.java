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

package io.github.pulsebeat02.deluxemediaplugin.command.video;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.CallbackBuilder;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.EntityType;
import io.github.pulsebeat02.ezmediacore.callback.Identifier;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.entity.NamedEntityString;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoBuilder;
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

	public @NotNull VideoPlayer createMapPlayer(@NotNull final Collection<? extends Player> viewers) {
		return VideoBuilder.unspecified()
				.callback(
						CallbackBuilder.map()
								.algorithm(this.attributes.getDitherType().getAlgorithm())
								.blockWidth(this.attributes.getPixelWidth())
								.map(Identifier.ofIdentifier(0))
								.dims(Dimension.ofDimension(this.attributes.getFrameWidth(), this.attributes.getFrameHeight()))
								.viewers(Viewers.ofPlayers(viewers))
								.delay(DelayConfiguration.DELAY_0_MS)
								.build(this.library))
				.dims(Dimension.ofDimension(this.attributes.getPixelWidth(), this.attributes.getPixelHeight()))
				.soundKey(SoundKey.ofSound("emc"))
				.build();
	}

	public @NotNull VideoPlayer createEntityPlayer(
			@NotNull final Player sender, @NotNull final Collection<? extends Player> viewers) {
		return VideoBuilder.unspecified()
				.callback(
						CallbackBuilder.entity()
								.character(NamedEntityString.NORMAL_SQUARE)
								.type(EntityType.ARMORSTAND)
								.location(sender.getLocation())
								.dims(Dimension.ofDimension(
										this.attributes.getPixelWidth(), this.attributes.getPixelHeight()))
								.viewers(Viewers.ofPlayers(viewers))
								.delay(DelayConfiguration.DELAY_20_MS)
								.build(this.library))
				.soundKey(SoundKey.ofSound("emc"))
				.build();
	}

	public @NotNull VideoPlayer createChatBoxPlayer(
			@NotNull final Collection<? extends Player> viewers) {
		return VideoBuilder.unspecified()
				.callback(
						CallbackBuilder.chat()
								.character(NamedEntityString.NORMAL_SQUARE)
								.dims(Dimension.ofDimension(
										this.attributes.getPixelWidth(), this.attributes.getPixelHeight()))
								.viewers(Viewers.ofPlayers(viewers))
								.delay(DelayConfiguration.ofDelay(20))
								.build(this.library))
				.soundKey(SoundKey.ofSound("emc"))
				.build();
	}

	public @NotNull VideoPlayer createScoreboardPlayer(
			@NotNull final Collection<? extends Player> viewers) {
		return VideoBuilder.unspecified()
				.callback(
						CallbackBuilder.scoreboard()
								.id(Identifier.ofIdentifier(1080))
								.dims(Dimension.ofDimension(
										this.attributes.getPixelWidth(), this.attributes.getPixelHeight()))
								.viewers(Viewers.ofPlayers(viewers))
								.delay(DelayConfiguration.DELAY_20_MS)
								.build(this.library))
				.soundKey(SoundKey.ofSound("emc"))
				.build();
	}

	public @NotNull VideoPlayer createBlockHighlightPlayer(
			@NotNull final Player sender) {
		return VideoBuilder.unspecified()
				.callback(
						CallbackBuilder.blockHighlight()
								.location(sender.getLocation())
								.dims(Dimension.ofDimension(
										this.attributes.getPixelWidth(), this.attributes.getPixelHeight()))
								.delay(DelayConfiguration.ofDelay(40))
								.build(this.library))
				.soundKey(SoundKey.ofSound("emc"))
				.build();
	}
}
