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
import io.github.pulsebeat02.deluxemediaplugin.discord.MediaBot;
import java.io.IOException;
import javax.security.auth.login.LoginException;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BotConfiguration extends ConfigurationProvider<MediaBot> {

  private MediaBot bot;

  public BotConfiguration(@NotNull final DeluxeMediaPlugin plugin) throws IOException {
    super(plugin, "configuration/bot.yml");
  }

  @Override
  void deserialize() {
    final FileConfiguration configuration = this.getFileConfiguration();
    configuration.set("token", this.bot.getJDA().getToken());
    this.saveConfig();
  }

  @Override
  void serialize() throws IOException {
    final DeluxeMediaPlugin plugin = this.getPlugin();
    final FileConfiguration configuration = this.getFileConfiguration();
    final String token = configuration.getString("token");
    if (token != null) {
      try {
        this.bot = new MediaBot(token);
      } catch (final LoginException | InterruptedException e) {
        plugin.getLogger().severe("A severe issue occurred while starting the bot. Please check the token!");
        e.printStackTrace();
      }
    } else {
      plugin.getLogger().info("Bot token for Discord bot not provided! Proceeding to disable bot!");
    }
  }

  @Override
  @Nullable
  public MediaBot getSerializedValue() {
    return this.bot;
  }
}
