package io.github.pulsebeat02.deluxemediaplugin.config;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class BotConfiguration extends ConfigurationProvider {

	public BotConfiguration(@NotNull DeluxeMediaPlugin plugin) throws IOException {
		super(plugin, "configuration/bot.yml");
	}

	@Override
	void deserialize() {
		final FileConfiguration configuration = this.getFileConfiguration();
		configuration.set("token", "");

		this.saveConfig();
	}

	@Override
	void serialize() throws IOException {
		final FileConfiguration configuration = this.getFileConfiguration();

	}
}
