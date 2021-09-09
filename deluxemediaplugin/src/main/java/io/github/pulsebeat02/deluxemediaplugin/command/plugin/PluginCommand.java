package io.github.pulsebeat02.deluxemediaplugin.command.plugin;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils;

import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public class PluginCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> node;

  private static final TextComponent PLUGIN_INFORMATION;

  static {
    PLUGIN_INFORMATION =
        text()
            .append(text("⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻", GOLD))
            .append(text("Plugin: ", GOLD, BOLD))
            .append(text("DeluxeMediaPlugin", AQUA))
            .append(newline())
            .append(text("Authors: ", GOLD, BOLD))
            .append(
                text(
                    "PulseBeat_02",
                    style(
                        AQUA,
                        text("PulseBeat_02's Github", GOLD).asHoverEvent(),
                        openUrl("https://github.com/PulseBeat02"))))
            .append(text(", ", GOLD))
            .append(
                text(
                    "itxfrosty",
                    style(
                        AQUA,
                        text("itxfrosty's Github", GOLD).asHoverEvent(),
                        openUrl("https://github.com/itxfrosty"))))
            .append(newline())
            .append(text("Version: ", GOLD, BOLD))
            .append(text("BETA Release", AQUA))
            .append(newline())
            .append(newline())
            .append(
                text(
                    "Click for Support Server",
                    style(
                        GOLD,
                        BOLD,
                        openUrl("https://discord.gg/AqK5dKdUZe"),
                        text("Click for Discord Server", GOLD).asHoverEvent())))
            .append(text("⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻⸻", GOLD))
            .append()
            .build();
  }

  public PluginCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(
        plugin,
        "deluxemediaplugin",
        executor,
        "deluxemediaplugin.command.deluxemediaplugin",
        "dmp");
    this.node =
        this.literal(this.getName())
            .requires(super::testPermission)
            .then(this.literal("info").executes(this::sendInformation))
            .build();
  }

  private int sendInformation(@NotNull final CommandContext<CommandSender> context) {
    this.plugin().audience().sender(context.getSource()).sendMessage(PLUGIN_INFORMATION);
    return SINGLE_SUCCESS;
  }

  @Override
  public @NotNull Component usage() {
    return ChatUtils.getCommandUsage(
        Map.of("/deluxemediaplugin", "Displays information about the plugin"));
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return this.node;
  }
}
