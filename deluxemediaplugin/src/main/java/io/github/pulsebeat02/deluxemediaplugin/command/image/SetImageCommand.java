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

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.rewrite.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.image.basic.StaticImage;
import io.github.pulsebeat02.minecraftmedialibrary.image.gif.DynamicImage;
import io.github.pulsebeat02.minecraftmedialibrary.utility.FileUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class SetImageCommand<S> implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final ImageCommandAttributes attributes;

  public SetImageCommand(@NotNull final ImageCommandAttributes attributes) {
    this.attributes = attributes;
    node =
        literal("set")
            .then(
                literal("map")
                    .then(
                        argument("id", LongArgumentType.longArg())
                            .then(
                                argument("mrl", StringArgumentType.greedyString())
                                    .executes(this::setImage))))
            .then(
                literal("dimensions")
                    .then(
                        argument("dims", StringArgumentType.greedyString())
                            .executes(this::setDimensions)))
            .build();
  }

  private int setImage(@NotNull final CommandContext<CommandSender> context) {
    final DeluxeMediaPlugin plugin = attributes.getPlugin();
    final Audience audience = plugin.audience().sender(context.getSource());
    final OptionalLong optional =
        ChatUtilities.checkMapBoundaries(audience, context.getArgument("id", String.class));
    if (!optional.isPresent()) {
      return SINGLE_SUCCESS;
    }
    final int id = (int) optional.getAsLong();
    final String mrl = context.getArgument("mrl", String.class);
    final ComponentLike successful =
        format(text(String.format("Successfully drew image on map %d", id), GOLD));
    final MediaLibrary library = plugin.getLibrary();
    final Set<String> extensions = attributes.getExtensions();
    final int width = attributes.getWidth();
    final int height = attributes.getHeight();
    if (isUrl(mrl)) {
      final Path img = FileUtilities.downloadImageFile(mrl, plugin.getLibrary().getParentFolder());
      final String name = PathUtilities.getName(img).toLowerCase();
      if (name.endsWith(".gif")) {
        new DynamicImage(library, id, img, width, height).drawImage();
      } else if (extensions.stream().anyMatch(name::endsWith)) {
        new StaticImage(library, id, img, width, height).drawImage();
      }
      audience.sendMessage(successful);
    } else {
      final Path img = Paths.get(plugin.getDataFolder().toString()).resolve(mrl);
      if (Files.exists(img)) {
        final String name = PathUtilities.getName(img).toLowerCase();
        if (name.endsWith(".gif")) {
          new StaticImage(library, id, img, width, height).drawImage();
        } else if (extensions.stream().anyMatch(name::endsWith)) {
          new DynamicImage(library, id, img, width, height).drawImage();
        }
        audience.sendMessage(successful);
      } else {
        audience.sendMessage(
            format(
                text(String.format("File %s cannot be found!", PathUtilities.getName(img)), RED)));
      }
    }
    return 1;
  }

  private int setDimensions(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = attributes.getPlugin().audience().sender(context.getSource());
    final Optional<int[]> optional =
        ChatUtilities.checkDimensionBoundaries(audience, context.getArgument("dims", String.class));
    if (!optional.isPresent()) {
      return SINGLE_SUCCESS;
    }
    final int[] dims = optional.get();
    attributes.setWidth(dims[0]);
    attributes.setHeight(dims[1]);
    audience.sendMessage(
        format(
            text(
                String.format(
                    "Changed itemframe dimensions to %d:%d (width:height)",
                    attributes.getWidth(), attributes.getHeight()))));
    return SINGLE_SUCCESS;
  }

  private boolean isUrl(@NotNull final String url) {
    return url.startsWith("http");
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> commandNode() {
    return node;
  }
}
