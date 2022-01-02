package io.github.pulsebeat02.deluxemediaplugin.command.video;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.command.Permission.has;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import io.github.pulsebeat02.ezmediacore.utility.concurrency.ThreadUtils;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class VideoDumpThreadsCommand implements CommandSegment.Literal<CommandSender> {

  private final DeluxeMediaPlugin plugin;
  private final LiteralCommandNode<CommandSender> node;

  public VideoDumpThreadsCommand(@NotNull final DeluxeMediaPlugin plugin) {
    this.plugin = plugin;
    this.node =
        this.literal("dump-threads")
            .requires(has("deluxemediaplugin.command.video.dumpthreads"))
            .executes(this::dumpThreads)
            .build();
  }

  private int dumpThreads(@NotNull final CommandContext<CommandSender> context) {
    this.createThreadDump();
    final Audience audience = this.plugin.audience().sender(context.getSource());
    audience.sendMessage(Locale.DUMP_THREADS.build());
    return SINGLE_SUCCESS;
  }

  private void createThreadDump() {
    ThreadUtils.createThreadDump();
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> getNode() {
    return this.node;
  }
}
