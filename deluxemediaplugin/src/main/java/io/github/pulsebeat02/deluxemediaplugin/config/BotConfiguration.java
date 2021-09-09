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
