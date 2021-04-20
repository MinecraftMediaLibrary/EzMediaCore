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

package com.github.pulsebeat02.deluxemediaplugin.command.audio;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import com.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper;
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting.HttpDaemonProvider;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> literalNode;
  private final AtomicBoolean atomicBoolean;
  private File audio;

  public AudioCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "audio", executor, "deluxemediaplugin.command.audio", "");
    final LiteralArgumentBuilder<CommandSender> builder = literal(getName());
    builder
        .requires(super::testPermission)
        .then(
            literal("load")
                .then(argument("mrl", StringArgumentType.greedyString()).executes(this::loadAudio)))
        .then(literal("play").executes(this::playAudio));
    literalNode = builder.build();
    atomicBoolean = new AtomicBoolean(false);
  }

  private int playAudio(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final MinecraftMediaLibrary library = getPlugin().getLibrary();
    if (audio == null) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("File and URL not Specified!", NamedTextColor.RED)));
      return 1;
    }
    if (!atomicBoolean.get()) {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text("The audio is still being downloaded!", NamedTextColor.RED)));
      return 1;
    }
    audience.sendMessage(
        ChatUtilities.formatMessage(Component.text("Started playing audio!", NamedTextColor.GOLD)));
    for (final Player p : Bukkit.getOnlinePlayers()) {
      p.playSound(p.getLocation(), getPlugin().getName().toLowerCase(), 1.0F, 1.0F);
    }
    return 1;
  }

  private int loadAudio(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final MinecraftMediaLibrary library = getPlugin().getLibrary();
    final String mrl = context.getArgument("mrl", String.class);
    if (mrl.startsWith("https://")) {
      final YoutubeExtraction extraction =
          new YoutubeExtraction(
              mrl,
              library.getAudioFolder().toString(),
              getPlugin().getEncoderConfiguration().getSettings());
      audio = extraction.extractAudio();
    } else {
      final File file = new File(mrl);
      if (file.exists()) {
        audio = file;
      } else {
        audience.sendMessage(
            ChatUtilities.formatMessage(
                Component.text(
                    "The mrl specified is not valid! It must be a Youtube link or an audio file.",
                    NamedTextColor.RED)));
        return 1;
      }
    }
    CompletableFuture.runAsync(
            () -> {
              final ResourcepackWrapper wrapper = ResourcepackWrapper.of(audio, library);
              wrapper.buildResourcePack();
              sendResourcepack(getPlugin().getHttpConfiguration().getDaemon(), audience, wrapper);
            })
        .whenCompleteAsync((t, throwable) -> atomicBoolean.set(true));
    return 1;
  }

  private void sendResourcepack(
      @Nullable final HttpDaemonProvider provider,
      @NotNull final Audience audience,
      @NotNull final ResourcepackWrapper wrapper) {
    if (provider != null) {
      final String url = provider.generateUrl(wrapper.getPath());
      for (final Player p : Bukkit.getOnlinePlayers()) {
        p.setResourcePack(url);
      }
    } else {
      audience.sendMessage(
          ChatUtilities.formatMessage(
              Component.text(
                  "You have HTTP set false by default. You cannot "
                      + "play Youtube videos without a daemon",
                  NamedTextColor.RED)));
    }
  }

  @Override
  public Component usage() {
    return null;
  }

  @Override
  public LiteralCommandNode<CommandSender> getCommandNode() {
    return literalNode;
  }
}
