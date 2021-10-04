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

package io.github.pulsebeat02.deluxemediaplugin.config;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.bot.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.message.Locale;
import java.io.IOException;
import javax.security.auth.login.LoginException;
import net.kyori.adventure.audience.Audience;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BotConfiguration extends ConfigurationProvider<MediaBot> {

  private MediaBot bot;

  public BotConfiguration(@NotNull final DeluxeMediaPlugin plugin) throws IOException {
    super(plugin, "configuration/bot.yml");
  }

  @Override
  public void deserialize() throws IOException {
    final FileConfiguration configuration = this.getFileConfiguration();
    configuration.set("token", this.bot.getJDA().getToken());
    this.saveConfig();
  }

  @Override
  public void serialize() {
    final DeluxeMediaPlugin plugin = this.getPlugin();
    final FileConfiguration configuration = this.getFileConfiguration();
    final Audience console = plugin.getConsoleAudience();

    boolean invalid = false;
    final String token = configuration.getString("token");
    if (token == null) {
      console.sendMessage(Locale.ERR_BOT_TOKEN.build());
      invalid = true;
    }

    final String guild = configuration.getString("guild-id");
    if (guild == null) {
      console.sendMessage(Locale.ERR_GUILD_TOKEN.build());
      invalid = true;
    }

    final String vc = configuration.getString("voice-chat-id");
    if (vc == null) {
      console.sendMessage(Locale.ERR_VC_ID.build());
      invalid = true;
    }

    if (!invalid) {
      try {
        this.bot = new MediaBot(token, guild, vc);
      } catch (final LoginException | InterruptedException e) {
        console.sendMessage(Locale.ERR_INVALID_DISCORD_BOT.build());
        e.printStackTrace();
      }
    }
  }

  @Override
  @Nullable
  public MediaBot getSerializedValue() {
    return this.bot;
  }
}
