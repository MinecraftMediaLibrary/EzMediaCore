package io.github.pulsebeat02.deluxemediaplugin.command.ffmpeg;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegCommandExecutor;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class FFmpegRunCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final DeluxeMediaPlugin plugin;
  private final FFmpegCommandExecutor ffmpeg;

  public FFmpegRunCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final FFmpegCommandExecutor executor) {
    this.plugin = plugin;
    this.ffmpeg = executor;
    this.node =
        this.literal("run")
            .requires(has("deluxemediaplugin.command.ffmpeg.run"))
            .executes(this::runFFmpegProcess)
            .build();
  }

  private int runFFmpegProcess(@NotNull final CommandContext<CommandSender> context) {

    final Audience audience = this.plugin.audience().sender(context.getSource());

    this.execute(audience);

    audience.sendMessage(Locale.FFMPEG_EXEC.build());

    return SINGLE_SUCCESS;
  }

  private void execute(@NotNull final Audience audience) {
    this.ffmpeg.executeWithLogging(s -> audience.sendMessage(Locale.FFMPEG_PROCESS.build(s)));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
