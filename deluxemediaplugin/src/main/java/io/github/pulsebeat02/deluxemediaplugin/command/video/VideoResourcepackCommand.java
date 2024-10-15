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
package io.github.pulsebeat02.deluxemediaplugin.command.video;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleNull;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleTrue;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.requiresPlayer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class VideoResourcepackCommand implements CommandSegment.Literal<CommandSender> {

  private static final List<String> SELECTOR_SUGGESTIONS;

  static {
    SELECTOR_SUGGESTIONS = List.of("@p", "@r", "@a", "@e", "@s");
  }

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  public VideoResourcepackCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.node =
        this.literal("resourcepack")
            .requires(has("deluxemediaplugin.command.video.resourcepack"))
            .then(
                this.literal("load")
                    .requires(has("deluxemediaplugin.video.resourcepack.load"))
                    .then(
                        this.argument("selectors", StringArgumentType.greedyString())
                            .suggests(this::suggestSelectors)
                            .executes(this::loadResourcepack)))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestSelectors(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    SELECTOR_SUGGESTIONS.forEach(builder::suggest);
    return builder.buildFuture();
  }

  private int loadResourcepack(@NotNull final CommandContext<CommandSender> context) {

    final CommandSender sender = context.getSource();
    final Audience audience = this.plugin.audience().sender(sender);
    final String targets = context.getArgument("selectors", String.class);

    if (this.attemptSelection(sender, audience, targets)) {
      return SINGLE_SUCCESS;
    }

    final List<Entity> entities =
        this.plugin.getBootstrap().getServer().selectEntities(sender, targets);
    if (this.checkSelectors(audience, entities)) {
      return SINGLE_SUCCESS;
    }

    if (requiresPlayer(this.plugin, sender)) {
      return SINGLE_SUCCESS;
    }

    if (this.unloadedResourcepack(audience)) {
      return SINGLE_SUCCESS;
    }

    this.loadResourcepack(entities, audience);

    return SINGLE_SUCCESS;
  }

  private boolean attemptSelection(
      @NotNull final CommandSender sender,
      @NotNull final Audience audience,
      @NotNull final String selector) {
    final boolean status;
    try {
      this.plugin.getBootstrap().getServer().selectEntities(sender, selector);
      return false;
    } catch (final IllegalArgumentException e) {
      status = true;
    }
    return handleTrue(audience, Locale.INVALID_TARGET_SELECTOR.build(), status);
  }

  private void loadResourcepack(
      @NotNull final List<Entity> entities, @NotNull final Audience audience) {
    final String url = this.config.getPackUrl();
    final byte[] hash = this.config.getPackHash();
    final List<? extends Player> cast = this.convert(entities);
    ResourcepackUtils.forceResourcepackLoad(this.plugin.library(), cast, url, hash);
    audience.sendMessage(Locale.RESOURCEPACK_INFO.build(url, hash));
  }

  private boolean unloadedResourcepack(@NotNull final Audience audience) {
    final Component component = Locale.INVALID_RESOURCEPACK.build();
    if (handleNull(audience, component, this.config.getPackUrl())) {
      return true;
    }
    return handleNull(audience, component, this.config.getPackHash());
  }

  private boolean checkSelectors(
      @NotNull final Audience audience, @NotNull final List<Entity> entities) {
    final boolean valid =
        entities.stream().anyMatch(Predicate.not(entity -> entity instanceof Player));
    return handleTrue(audience, Locale.INVALID_TARGET_SELECTOR.build(), valid);
  }

  private List<? extends Player> convert(@NotNull final List<Entity> entities) {
    return entities.stream().map(entity -> (Player) entity).collect(Collectors.toList());
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
