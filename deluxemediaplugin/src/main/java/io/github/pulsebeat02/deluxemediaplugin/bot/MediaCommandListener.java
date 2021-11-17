/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.deluxemediaplugin.bot;

import io.github.pulsebeat02.deluxemediaplugin.bot.command.ConnectAudioCommand;
import io.github.pulsebeat02.deluxemediaplugin.bot.command.DisconnectAudioCommand;
import io.github.pulsebeat02.deluxemediaplugin.bot.command.DiscordBaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.bot.command.PlayAudioCommand;
import io.github.pulsebeat02.deluxemediaplugin.bot.command.StopAudioCommand;
import io.github.pulsebeat02.ezmediacore.utility.collection.ArrayUtils;
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
            "disconnect", new DisconnectAudioCommand(bot),
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
