package io.github.pulsebeat02.deluxemediaplugin.command.ffmpeg;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegCommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class FFmpegListArgumentsCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final DeluxeMediaPlugin plugin;
  private final FFmpegCommandExecutor ffmpeg;

  public FFmpegListArgumentsCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final FFmpegCommandExecutor executor) {
    this.plugin = plugin;
    this.ffmpeg = executor;
    this.node =
        this.literal("list-arguments")
            .requires(has("deluxemediaplugin.command.ffmpeg.listarguments"))
            .executes(this::listFFmpegArguments)
            .build();
  }

  private int listFFmpegArguments(@NotNull final CommandContext<CommandSender> context) {

    this.plugin
        .audience()
        .sender(context.getSource())
        .sendMessage(Locale.LIST_FFMPEG_ARGS.build(this.ffmpeg.getArguments()));

    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
