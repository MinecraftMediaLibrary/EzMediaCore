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

package io.github.pulsebeat02.deluxemediaplugin.command.image;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.executors.FixedExecutors.RESOURCE_WRAPPER_EXECUTOR;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleEmptyOptional;
import static java.util.Objects.requireNonNull;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.deluxemediaplugin.utility.component.ChatUtils;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.image.DynamicImage;
import io.github.pulsebeat02.ezmediacore.image.Image;
import io.github.pulsebeat02.ezmediacore.image.StaticImage;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import io.github.pulsebeat02.ezmediacore.utility.io.PathUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

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
                        this.argument("mrl", StringArgumentType.greedyString())
                            .executes(this::setImage)))
            .then(
                this.literal("dimensions")
                    .then(
                        this.argument("dims", StringArgumentType.greedyString())
                            .executes(this::setDimensions)))
            .build();
  }

  private int setImage(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    final int width = this.attributes.getWidth();
    final int height = this.attributes.getHeight();
    final Optional<ImageMrlType> optional = ImageMrlType.getType(mrl);
    if (handleEmptyOptional(audience, Locale.ERR_INVALID_EXTENSION.build(), optional)) {
      return SINGLE_SUCCESS;
    }

    audience.sendMessage(Locale.LOAD_IMG.build());

    final ImageMrlType type = optional.get();

    CompletableFuture.runAsync(
        () -> this.handleImageType(audience, mrl, width, height, type), RESOURCE_WRAPPER_EXECUTOR)
        .handle(Throwing.THROWING_FUTURE);

    return SINGLE_SUCCESS;
  }

  private void handleImageType(
      @NotNull final Audience audience,
      @NotNull final String mrl,
      final int width, final int height,
      @NotNull final ImageMrlType type) {
    switch (type) {
      case LOCAL_FILE -> this.handleLocalFile(mrl, width, height);
      case DIRECT_LINK -> this.handleWebFile(mrl, width, height);
      default -> throw new IllegalArgumentException("Illegal image type!");
    }
    audience.sendMessage(Locale.DREW_IMG.build(mrl));
  }

  private void handleLocalFile(@NotNull final String mrl, final int width, final int height) {
    try {
      this.drawImage(Path.of(mrl), width, height);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void handleWebFile(@NotNull final String mrl, final int width, final int height) {
    try {
      final Path output = this.plugin.library().getLibraryPath();
      final Path download = FileUtils.downloadImageFile(mrl, output);
      this.drawImage(download, width, height);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void drawImage(
      @NotNull final Path img,
      final int width, final int height)
      throws IOException {

    final MediaLibraryCore core = this.plugin.library();
    final List<Integer> maps = this.getMapsFromDimension(width, height);
    final String name = PathUtils.getName(img).toLowerCase();
    final Image image = this.getProperImage(img, width, height, core, maps, name);

    image.draw(true);

    this.plugin.getPictureManager().getImages().add(image);
  }

  @NotNull
  private Image getProperImage(
      @NotNull final Path img,
      final int width, final int height,
      @NotNull final MediaLibraryCore core,
      @NotNull final List<Integer> maps,
      @NotNull final String name) throws IOException {
    return name.endsWith("gif") ? new DynamicImage(core, img, maps,
        Dimension.ofDimension(width, height)) :
        new StaticImage(core, img, maps, Dimension.ofDimension(width, height));
  }

  private @NotNull List<Integer> getMapsFromDimension(final int width, final int height) {
    final List<Integer> maps = new ArrayList<>();
    final Server server = this.plugin.getBootstrap().getServer();
    for (int i = 0; i < width * height; i++) {
      maps.add(server.createMap(requireNonNull(server.getWorld("world"))).getId());
    }
    return maps;
  }

  private int setDimensions(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final Optional<int[]> optional =
        ChatUtils.checkDimensionBoundaries(audience, context.getArgument("dims", String.class));
    if (optional.isEmpty()) {
      return SINGLE_SUCCESS;
    }

    final int[] dims = optional.get();
    final int width = dims[0];
    final int height = dims[1];

    this.attributes.setWidth(width);
    this.attributes.setHeight(height);

    audience.sendMessage(Locale.CHANGED_IMG_DIMS.build(width, height));

    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
