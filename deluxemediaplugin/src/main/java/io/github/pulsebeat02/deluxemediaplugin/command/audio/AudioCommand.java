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

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class AudioCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> node;
  private final AudioCommandAttributes attributes;

  public AudioCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "audio", executor, "deluxemediaplugin.command.audio");
    attributes = new AudioCommandAttributes(plugin);
    node =
        literal(getName())
            .requires(super::testPermission)
            .then(new AudioLoadCommand(plugin, attributes).node())
            .then(literal("play").executes(this::playAudio))
            .then(literal("stop").executes(this::stopAudio))
            .build();
  }

  private int playAudio(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin().audience().sender(context.getSource());
    if (checkUnloaded(audience)) {
      return SINGLE_SUCCESS;
    }
    if (checkIncompleteLoad(audience)) {
      return SINGLE_SUCCESS;
    }
    Bukkit.getOnlinePlayers()
        .forEach(
            p ->
                p.playSound(
                    p.getLocation(), attributes.getSoundKey(), SoundCategory.MUSIC, 100.0F, 1.0F));
    audience.sendMessage(format(text("Started playing audio!", GOLD)));
    return SINGLE_SUCCESS;
  }

  private int stopAudio(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin().audience().sender(context.getSource());
    if (checkUnloaded(audience)) {
      return SINGLE_SUCCESS;
    }
    if (checkIncompleteLoad(audience)) {
      return SINGLE_SUCCESS;
    }
    Bukkit.getOnlinePlayers().forEach(p -> p.stopSound(attributes.getSoundKey()));
    audience.sendMessage(format(text("Stopped playing audio!", RED)));
    return SINGLE_SUCCESS;
  }

  private boolean checkUnloaded(@NotNull final Audience audience) {
    if (attributes.getResourcepackAudio() == null) {
      audience.sendMessage(format(text("File or URL not specified!", RED)));
      return true;
    }
    return false;
  }

  private boolean checkIncompleteLoad(@NotNull final Audience audience) {
    if (!attributes.getCompletion().get()) {
      audience.sendMessage(format(text("Audio is still processing!", RED)));
      return true;
    }
    return false;
  }

  @Override
  public Component usage() {
    return ChatUtils.getCommandUsage(
        ImmutableMap.<String, String>builder()
            .put("/audio load [url]", "Loads a Youtube Link")
            .put("/audio load [file]", "Loads a specific audio file")
            .put("/audio load resourcepack", "Loads the past resourcepack used for the audio")
            .put("/audio play", "Plays the audio to players")
            .put("/audio stop", "Stops the audio")
            .build());
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return node;
  }
}
