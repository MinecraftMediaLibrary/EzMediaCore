package io.github.pulsebeat02.deluxemediaplugin.discord;

import io.github.pulsebeat02.deluxemediaplugin.discord.command.ConnectAudioCommand;
import io.github.pulsebeat02.deluxemediaplugin.discord.command.DiscordBaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.discord.command.PlayAudioCommand;
import io.github.pulsebeat02.deluxemediaplugin.discord.command.StopAudioCommand;
import io.github.pulsebeat02.ezmediacore.utility.ArrayUtils;
import java.util.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class MediaCommandListener {

  private final MediaBot bot;
  private final Map<String, DiscordBaseCommand> commands;

  public MediaCommandListener(@NotNull final MediaBot bot) {
    this.bot = bot;
    this.commands =
        Map.of(
            "connect", new ConnectAudioCommand(bot),
            "play", new PlayAudioCommand(bot),
            "stop", new StopAudioCommand(bot));
  }

  @SubscribeEvent
  public void onMessageReceivedEvent(@NotNull final MessageReceivedEvent event) {
    final String message = event.getMessage().getContentRaw();
    final String prefix = this.bot.getPrefix();
    if (message.startsWith(prefix)) {
      final User user = event.getAuthor();
      final Message msg = event.getMessage();
      if (!this.canExecuteCommand(user)) {
        msg.getChannel()
            .sendMessageEmbeds(
                new EmbedBuilder()
                    .setTitle("Not Enough Permissions")
                    .setDescription(
                        "You must have administrator permissions or the DJ role to execute this command!")
                    .build())
            .queue();
      }
      final String[] content = message.substring(prefix.length()).split(" ");
      final DiscordBaseCommand command = this.commands.get(content[0]);
      if (command != null) {
        command.execute(msg, ArrayUtils.trim(content, 1, content.length));
      }
    }
  }

  private boolean canExecuteCommand(@NotNull final User user) {
    final Member member = this.bot.getGuild().getMember(user);
    if (member != null) {
      return member.isOwner()
          || member.hasPermission(Permission.ADMINISTRATOR)
          || member.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("DJ"));
    }
    return false;
  }

  public @NotNull MediaBot getBot() {
    return this.bot;
  }
}
