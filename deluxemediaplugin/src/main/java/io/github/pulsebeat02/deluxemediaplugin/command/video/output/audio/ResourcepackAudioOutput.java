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
package io.github.pulsebeat02.deluxemediaplugin.command.video.output.audio;

import static org.bukkit.SoundCategory.MASTER;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import java.util.Set;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResourcepackAudioOutput extends AudioOutput {

  public ResourcepackAudioOutput() {
    super("RESOURCEPACK");
  }

  @Override
  public void setAudioHandler(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final ScreenConfig attributes,
      @NotNull final Audience audience,
      @NotNull final String mrl) {}

  @Override
  public void setProperAudioHandler(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig attributes) {
    final VideoPlayer player = attributes.getPlayer();
    final Set<Player> viewers = player.getWatchers().getPlayers();
    final String sound = player.getSoundKey().getName();
    viewers.forEach(p -> p.playSound(p.getLocation(), sound, MASTER, 100.0F, 1.0F));
  }
}
