package io.github.pulsebeat02.deluxemediaplugin.command.video.load;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;
import static io.github.pulsebeat02.deluxemediaplugin.utility.nullability.ArgumentUtils.handleTrue;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.command.video.ScreenConfig;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.PathInput;
import io.github.pulsebeat02.ezmediacore.utility.io.PathUtils;
import java.nio.file.Path;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class LoadPathCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;
  private final ScreenConfig config;
  private final LoadVideoCommand command;

  public LoadPathCommand(
      @NotNull final DeluxeMediaPlugin plugin,
      @NotNull final ScreenConfig config,
      @NotNull final LoadVideoCommand command) {
    this.plugin = plugin;
    this.config = config;
    this.command = command;
    this.node =
        this.literal("file")
            .requires(has("deluxemediaplugin.command.video.load.file"))
            .then(
                this.argument("path", StringArgumentType.greedyString()).executes(this::handleFile))
            .build();
  }

  private int handleFile(final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());
    final String path = context.getArgument("path", String.class);

    if (this.checkInvalidPath(audience, path)) {
      return SINGLE_SUCCESS;
    }

    this.setPathMedia(Path.of(path));

    audience.sendMessage(Locale.SET_MEDIA.build("File path"));

    this.command.loadVideo(audience);

    return SINGLE_SUCCESS;
  }

  private void setPathMedia(@NotNull final Path path) {
    this.config.setMedia(PathInput.ofPath(path));
  }

  private boolean checkInvalidPath(@NotNull final Audience audience, @NotNull final String path) {
    return handleTrue(audience, Locale.ERR_INVALID_PATH.build(), PathUtils.isValidPath(path));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
