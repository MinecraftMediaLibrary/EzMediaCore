package io.github.pulsebeat02.deluxemediaplugin.command.video.set;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleEmptyOptional;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.output.DitheringAlgorithm;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class SetDitheringAlgorithmCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;

  public SetDitheringAlgorithmCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final ScreenConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.node =
        this.literal("dithering-algorithm")
            .requires(has("deluxemediaplugin.video.set.ditheringalgorithm"))
            .then(
                this.argument("algorithm", StringArgumentType.word())
                    .suggests(this::suggestDitheringAlgorithm)
                    .executes(this::setDitheringAlgorithm))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestDitheringAlgorithm(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    Arrays.stream(DitheringAlgorithm.values())
        .forEach(algorithm -> builder.suggest(algorithm.name()));
    return builder.buildFuture();
  }

  private int setDitheringAlgorithm(final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String algorithm = context.getArgument("algorithm", String.class);
    final Optional<DitheringAlgorithm> setting = DitheringAlgorithm.ofKey(algorithm);

    if (handleEmptyOptional(audience, Locale.ERR_INVALID_DITHER_ALGORITHM.build(algorithm), setting)) {
      return SINGLE_SUCCESS;
    }

    this.setDitheringAlgorithm(setting.get());

    audience.sendMessage(Locale.SET_DITHER_ALGORITHM.build(algorithm));

    return SINGLE_SUCCESS;
  }

  private void setDitheringAlgorithm(@NotNull final DitheringAlgorithm setting) {
    this.config.setDitheringAlgorithm(setting);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
