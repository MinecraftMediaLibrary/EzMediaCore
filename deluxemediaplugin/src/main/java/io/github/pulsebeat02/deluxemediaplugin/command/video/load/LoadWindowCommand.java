package io.github.pulsebeat02.deluxemediaplugin.command.video.load;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleFalse;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.player.buffered.FFmpegMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.WindowInput;
import io.github.pulsebeat02.ezmediacore.utility.graphics.WindowUtils;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class LoadWindowCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;
  private final LoadVideoCommand command;

  public LoadWindowCommand(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final ScreenConfig config,
      @NotNull final LoadVideoCommand command) {
    this.plugin = plugin;
    this.config = config;
    this.command = command;
    this.node =
        this.literal("window")
            .requires(has("deluxemediaplugin.command.video.load.window"))
            .then(
                this.argument("title", StringArgumentType.greedyString())
                    .suggests(this::suggestWindow)
                    .executes(this::handleWindow))
            .build();
  }

  private @NotNull CompletableFuture<Suggestions> suggestWindow(
      @NotNull final CommandContext<CommandSender> context,
      @NotNull final SuggestionsBuilder builder) {
    WindowUtils.getAllWindows().forEach(window -> builder.suggest(window.getTitle()));
    return builder.buildFuture();
  }

  private int handleWindow(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String name = context.getArgument("title", String.class);

    if (this.checkInvalidPlayer(audience)) {
      return SINGLE_SUCCESS;
    }

    if (this.checkInvalidOperatingSystem(audience)) {
      return SINGLE_SUCCESS;
    }

    if (this.checkInvalidWindow(audience, name)) {
      return SINGLE_SUCCESS;
    }

    this.setWindowMedia(name);

    audience.sendMessage(Locale.SET_MEDIA.build("Window capture"));

    this.command.loadVideo(audience);

    return SINGLE_SUCCESS;
  }

  private void setWindowMedia(@NotNull final String name) {
    this.config.setMedia(WindowInput.ofWindowName(name));
  }

  private boolean checkInvalidOperatingSystem(@NotNull final Audience audience) {
    return handleFalse(
        audience, Locale.ERR_INVALID_OS.build(), OSType.getCurrentOS() == OSType.WINDOWS);
  }

  private boolean checkInvalidPlayer(@NotNull final Audience audience) {
    return handleFalse(
        audience,
        Locale.ERR_INVALID_PLAYER_MEDIA.build("JCodec and VLC Media Player", "Window capture"),
        this.config.getPlayer() instanceof FFmpegMediaPlayer);
  }

  private boolean checkInvalidWindow(@NotNull final Audience audience, @NotNull final String name) {
    final String target = name.toUpperCase(java.util.Locale.ROOT);
    final boolean found =
        WindowUtils.getAllWindows().stream()
            .anyMatch(
                window -> window.getTitle().toUpperCase(java.util.Locale.ROOT).contains(target));
    return handleFalse(audience, Locale.ERR_INVALID_WINDOW.build(), found);
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
