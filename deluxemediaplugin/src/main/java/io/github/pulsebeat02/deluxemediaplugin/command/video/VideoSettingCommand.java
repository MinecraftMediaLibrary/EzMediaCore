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

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.dither.DitherSetting;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.TextComponent.ofChildren;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class VideoSettingCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final VideoCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;

  public VideoSettingCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final VideoCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    node =
        literal("set")
            .then(
                literal("screen-dimension")
                    .then(
                        argument("screen-dimensions", StringArgumentType.greedyString())
                            .executes(this::setScreenDimensions)))
            .then(
                literal("itemframe-dimension")
                    .then(
                        argument("itemframe-dimensions", StringArgumentType.greedyString())
                            .executes(this::setItemframeDimensions)))
            .then(
                literal("starting-map")
                    .then(
                        argument(
                                "map-id",
                                IntegerArgumentType.integer(-2_147_483_647, 2_147_483_647))
                            .executes(this::setStartingMap)))
            .then(
                literal("dither")
                    .then(
                        argument("dithering-option", StringArgumentType.word())
                            .suggests(this::suggestDitheringOptions)
                            .executes(this::setDitherMode)))
            .then(
                literal("mode")
                    .then(
                        argument("video-mode", StringArgumentType.word())
                            .suggests(this::suggestVideoModes)
                            .executes(this::setMode)))
            .build();
  }

  private CompletableFuture<Suggestions> suggestDitheringOptions(
      final CommandContext<CommandSender> context, final SuggestionsBuilder builder) {
    Arrays.stream(DitherSetting.values()).forEach(x -> builder.suggest(x.name()));
    return builder.buildFuture();
  }

  private CompletableFuture<Suggestions> suggestVideoModes(
      final CommandContext<CommandSender> context, final SuggestionsBuilder builder) {
    Arrays.stream(VideoType.values()).forEach(x -> builder.suggest(x.getName()));
    return builder.buildFuture();
  }

  private int setScreenDimensions(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin.audience().sender(context.getSource());
    final Optional<int[]> optional =
        ChatUtils.checkDimensionBoundaries(
            audience, context.getArgument("screen-dimensions", String.class));
    if (!optional.isPresent()) {
      return SINGLE_SUCCESS;
    }
    final int[] dimensions = optional.get();
    attributes.setScreenWidth(dimensions[0]);
    attributes.setScreenHeight(dimensions[1]);
    audience.sendMessage(
        format(
            ofChildren(
                text("Set screen dimensions to ", GOLD),
                text(
                    String.format(
                        "%d:%d ", attributes.getScreenWidth(), attributes.getScreenHeight()),
                    AQUA),
                text("(width:height)", GOLD))));
    return SINGLE_SUCCESS;
  }

  private int setItemframeDimensions(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin.audience().sender(context.getSource());
    final Optional<int[]> optional =
        ChatUtils.checkDimensionBoundaries(
            audience, context.getArgument("itemframe-dimensions", String.class));
    if (!optional.isPresent()) {
      return SINGLE_SUCCESS;
    }
    final int[] dims = optional.get();
    attributes.setFrameWidth(dims[0]);
    attributes.setFrameHeight(dims[1]);
    audience.sendMessage(
        format(
            ofChildren(
                text("Set itemframe map dimensions to ", GOLD),
                text(
                    String.format(
                        "%s:%s ", attributes.getFrameHeight(), attributes.getFrameWidth()),
                    AQUA),
                text("(width:height)", GOLD))));
    return 1;
  }

  private int setStartingMap(@NotNull final CommandContext<CommandSender> context) {
    attributes.setStartingMap(context.getArgument("map-id", int.class));
    plugin
        .audience()
        .sender(context.getSource())
        .sendMessage(
            format(
                ofChildren(
                    text("Set starting map id to ", GOLD),
                    text(attributes.getStartingMap(), AQUA))));
    return SINGLE_SUCCESS;
  }

  private int setDitherMode(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin.audience().sender(context.getSource());
    final String algorithm = context.getArgument("dithering-option", String.class);
    final DitherSetting setting = DitherSetting.fromString(algorithm);
    if (setting == null) {
      audience.sendMessage(
          format(text(String.format("Could not find dither type %s", algorithm), RED)));
    } else {
      audience.sendMessage(
          format(ofChildren(text("Set dither type to ", GOLD), text(algorithm, AQUA))));
      attributes.setDither(setting);
    }
    return SINGLE_SUCCESS;
  }

  private int setMode(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin.audience().sender(context.getSource());
    final String mode = context.getArgument("video-mode", String.class);
    final VideoType type = VideoType.fromString(mode);
    if (type == null) {
      audience.sendMessage(format(text(String.format("Could not find video mode %s", mode), RED)));
    } else {
      attributes.setVideoType(type);
      audience.sendMessage(format(ofChildren(text("Set video mode to ", GOLD), text(mode, AQUA))));
    }
    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return node;
  }
}
