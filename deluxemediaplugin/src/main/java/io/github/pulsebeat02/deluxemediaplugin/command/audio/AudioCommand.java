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

package io.github.pulsebeat02.deluxemediaplugin.command.audio;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.gold;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.red;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils;

import java.util.Map;
import java.util.function.Consumer;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class AudioCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> node;
  private final AudioCommandAttributes attributes;

  public AudioCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "audio", executor, "deluxemediaplugin.command.audio");
    this.attributes = new AudioCommandAttributes(plugin);
    this.node =
        this.literal(this.getName())
            .requires(super::testPermission)
            .then(new AudioLoadCommand(plugin, this.attributes).node())
            .then(this.literal("play").executes(this::playAudio))
            .then(this.literal("stop").executes(this::stopAudio))
            .build();
  }

  private int playAudio(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin().audience().sender(context.getSource());

    if (this.checkUnloaded(audience) || this.checkIncompleteLoad(audience)) {
      return SINGLE_SUCCESS;
    }

    this.audioAction(
        player ->
            player.playSound(
                player.getLocation(),
                this.attributes.getKey(),
                SoundCategory.MASTER,
                100.0F,
                1.0F));

    gold(audience, "Started playing audio!");

    return SINGLE_SUCCESS;
  }

  private int stopAudio(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin().audience().sender(context.getSource());

    if (this.checkUnloaded(audience) || this.checkIncompleteLoad(audience)) {
      return SINGLE_SUCCESS;
    }

    this.audioAction(player -> player.stopSound(this.attributes.getKey()));

    red(audience, "Stopped playing audio!");

    return SINGLE_SUCCESS;
  }

  private void audioAction(@NotNull final Consumer<Player> consumer) {
    Bukkit.getOnlinePlayers().forEach(consumer);
  }

  private boolean checkUnloaded(@NotNull final Audience audience) {
    if (this.attributes.getAudio() == null) {
      red(audience, "File or URL not specified!");
      return true;
    }
    return false;
  }

  private boolean checkIncompleteLoad(@NotNull final Audience audience) {
    if (!this.attributes.getCompletion().get()) {
      red(audience, "Audio is still processing!");
      return true;
    }
    return false;
  }

  @Override
  public @NotNull Component usage() {
    return ChatUtils.getCommandUsage(
        Map.of(
            "/audio load [url]", "Loads a Youtube Link",
            "/audio load [file]", "Loads a specific audio file",
            "/audio load resourcepack", "Loads the past resourcepack used for the audio",
            "/audio play", "Plays the audio to players",
            "/audio stop", "Stops the audio"));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
