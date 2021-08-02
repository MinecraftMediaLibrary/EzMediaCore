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

package io.github.pulsebeat02.deluxemediaplugin.command.image;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.image.DynamicImage;
import io.github.pulsebeat02.ezmediacore.utility.FileUtils;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import io.github.pulsebeat02.minecraftmedialibrary.image.basic.StaticImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class SetImageCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final ImageCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;

  public SetImageCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ImageCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    this.node =
            this.literal("set")
            .then(
                    this.literal("map")
                    .then(
                            this.argument("id", IntegerArgumentType.integer(-2_147_483_647, 2_147_483_647))
                            .then(
                                    this.argument("mrl", StringArgumentType.greedyString())
                                    .executes(this::setImage))))
            .then(
                    this.literal("dimensions")
                    .then(
                            this.argument("dims", StringArgumentType.word()).executes(this::setDimensions)))
            .build();
  }

  private int setImage(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.plugin.audience().sender(context.getSource());
    final int id = context.getArgument("id", int.class);
    final String mrl = context.getArgument("mrl", String.class);
    final ComponentLike successful =
        format(text(String.format("Successfully drew image on map %d", id), GOLD));
    final MediaLibraryCore library = this.plugin.library();
    final Set<String> extensions = this.attributes.getExtensions();
    final int width = this.attributes.getWidth();
    final int height = this.attributes.getHeight();
    if (this.isUrl(mrl)) {
      try {
        final Path img = FileUtils.downloadImageFile(mrl, this.plugin.library().getLibraryPath());
        final String name = PathUtils.getName(img).toLowerCase();
        if (name.endsWith(".gif")) {
          new DynamicImage(library, id, img, width, height).drawImage();
        } else if (extensions.stream().anyMatch(name::endsWith)) {
          new StaticImage(library, id, img, width, height).drawImage();
        }
        audience.sendMessage(successful);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    } else {
      final Path img = Paths.get(this.plugin.getDataFolder().toString()).resolve(mrl);
      if (Files.exists(img)) {
        final String name = PathUtils.getName(img).toLowerCase();
        if (name.endsWith(".gif")) {
          new StaticImage(library, id, img, width, height).drawImage();
        } else if (extensions.stream().anyMatch(name::endsWith)) {
          new DynamicImage(library, id, img, width, height).drawImage();
        }
        audience.sendMessage(successful);
      } else {
        audience.sendMessage(
            format(text(String.format("File %s cannot be found!", PathUtils.getName(img)), RED)));
      }
    }
    return 1;
  }

  private int setDimensions(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = this.plugin.audience().sender(context.getSource());
    final Optional<int[]> optional =
        ChatUtils.checkDimensionBoundaries(audience, context.getArgument("dims", String.class));
    if (!optional.isPresent()) {
      return SINGLE_SUCCESS;
    }
    final int[] dims = optional.get();
    this.attributes.setWidth(dims[0]);
    this.attributes.setHeight(dims[1]);
    audience.sendMessage(
        format(
            text(
                String.format(
                    "Changed itemframe dimensions to %d:%d (width:height)",
                        this.attributes.getWidth(), this.attributes.getHeight()))));
    return SINGLE_SUCCESS;
  }

  private boolean isUrl(@NotNull final String url) {
    return url.startsWith("http");
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
