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
import io.github.pulsebeat02.ezmediacore.utility.io.ResourcepackUtils;
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

  private void loadResourcepack(
      @NotNull final List<Entity> entities, @NotNull final Audience audience) {
    final String url = this.config.getPackUrl();
    final byte[] hash = this.config.getPackHash();
    final List<? extends Player> cast = this.convert(entities);
    ResourcepackUtils.forceResourcepackLoad(this.plugin.library(), cast, url, hash);
    audience.sendMessage(Locale.SENT_RESOURCEPACK.build(url, hash));
  }

  private boolean unloadedResourcepack(@NotNull final Audience audience) {
    final Component component = Locale.ERR_NO_RESOURCEPACK.build();
    if (handleNull(audience, component, this.config.getPackUrl())) {
      return true;
    }
    return handleNull(audience, component, this.config.getPackHash());
  }

  private boolean checkSelectors(
      @NotNull final Audience audience, @NotNull final List<Entity> entities) {
    final boolean valid = entities.stream()
        .anyMatch(Predicate.not(entity -> entity instanceof Player));
    return handleTrue(
        audience,
        Locale.ERR_INVALID_TARGET_SELECTOR.build(),
        valid);
  }

  private List<? extends Player> convert(@NotNull final List<Entity> entities) {
    return entities.stream().map(entity -> (Player) entity).collect(Collectors.toList());
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
