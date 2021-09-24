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

package io.github.pulsebeat02.deluxemediaplugin;

import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtils.format;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import io.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandHandler;
import io.github.pulsebeat02.deluxemediaplugin.config.BotConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.EncoderConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.HttpConfiguration;
import io.github.pulsebeat02.deluxemediaplugin.config.PersistentPictureManager;
import io.github.pulsebeat02.deluxemediaplugin.discord.MediaBot;
import io.github.pulsebeat02.deluxemediaplugin.update.UpdateChecker;
import io.github.pulsebeat02.deluxemediaplugin.utility.CommandUtils;
import io.github.pulsebeat02.ezmediacore.LibraryProvider;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.sneaky.ThrowingConsumer;
import io.github.pulsebeat02.ezmediacore.utility.FileUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DeluxeMediaPlugin {

	private final JavaPlugin plugin;

	private MediaLibraryCore library;
	private BukkitAudiences audiences;
	private CommandHandler handler;
	private Logger logger;

	private PersistentPictureManager manager;
	private AudioConfiguration audioConfiguration;
	private HttpServer server;
	private MediaBot mediaBot;

	public DeluxeMediaPlugin(@NotNull final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void enable() {
		this.logger = this.plugin.getLogger();
		this.audiences = BukkitAudiences.create(this.plugin);
		this.printLogo();
		this.log(join(separator(text(" ")), text("Running DeluxeMediaPlugin", AQUA), text("[BETA]", GOLD), text("1.0.0", AQUA)));
		this.log("Loading MinecraftMediaLibrary instance... this may take a minute depending on your server!");
		try {
			this.library = LibraryProvider.builder().plugin(this.plugin).build();
			this.library.initialize();
		} catch (final ExecutionException | InterruptedException e) {
			this.log(text("There was a severe issue while loading the EzMediaCore instance!", RED));
			e.printStackTrace();
			this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
			return;
		}
		this.log("Finished loading MinecraftMediaLibrary instance!");
		this.loadPersistentData();
		this.log("Finished loading persistent data!");
		this.registerCommands();
		this.log("Finished registering plugin commands!");
		this.startMetrics();
		this.log("Finished loading Metrics data!");
		this.checkUpdates();
		this.log("Finished loading DeluxeMediaPlugin!");
		this.log("""
				Hello %%__USER__%%! Thank you for purchasing DeluxeMediaPlugin. For identifier purposes, this
				 is your purchase identification code: %%__NONCE__%% - Enjoy using the plugin, and ask for
				 support at my Discord! (https://discord.gg/MgqRKvycMC)
				""");
	}

	private void startMetrics() {
		new Metrics(this.plugin, 10229);
	}

	private void checkUpdates() {
		new UpdateChecker(this).check();
	}

	private void printLogo() {
		final List<String> logo =
				Arrays.asList(
						" _____       _                __  __          _ _       _____  _             _       ",
						" |  __ \\     | |              |  \\/  |        | (_)     |  __ \\| |           (_)      ",
						" | |  | | ___| |_   ___  _____| \\  / | ___  __| |_  __ _| |__) | |_   _  __ _ _ _ __  ",
						" | |  | |/ _ \\ | | | \\ \\/ / _ \\ |\\/| |/ _ \\/ _` | |/ _` |  ___/| | | | |/ _` | | '_ \\ ",
						" | |__| |  __/ | |_| |>  <  __/ |  | |  __/ (_| | | (_| | |    | | |_| | (_| | | | | |",
						" |_____/ \\___|_|\\__,_/_/\\_\\___|_|  |_|\\___|\\__,_|_|\\__,_|_|    |_|\\__,_|\\__, |_|_| |_|",
						"                                                                         __/ |        ",
						"                                                                        |___/         ");
		for (final String line : logo) {
			this.audiences.console().sendMessage(text(line, BLUE));
		}
	}

	public void disable() {
		this.log("DeluxeMediaPlugin is shutting down!");
		if (this.library != null) {
			this.library.shutdown();
			this.log("Successfully shutdown MinecraftMediaLibrary instance!");
		} else {
			this.log(text("EzMediaCore instance is null... something fishy is going on.", RED));
		}
		if (this.handler != null) {
				for (final BaseCommand cmd : this.handler.getCommands()) {
					CommandUtils.unRegisterBukkitCommand(this, cmd);
				}
		}
		if (this.mediaBot != null) {
			this.mediaBot.getJDA().shutdown();
		}
		this.log("Good Bye :(");
	}

	private void loadPersistentData() {
		try {
			Set.of(this.plugin.getDataFolder().toPath().resolve("configuration")).forEach(
					ThrowingConsumer.unchecked(FileUtils::createFolderIfNotExists));
			final HttpConfiguration httpConfiguration = new HttpConfiguration(this);
			final EncoderConfiguration encoderConfiguration = new EncoderConfiguration(this);
			final BotConfiguration botConfiguration = new BotConfiguration(this);
			httpConfiguration.read();
			encoderConfiguration.read();
			botConfiguration.read();
			this.server = httpConfiguration.getSerializedValue();
			this.audioConfiguration = encoderConfiguration.getSerializedValue();
			this.mediaBot = botConfiguration.getSerializedValue();
			this.manager = new PersistentPictureManager(this);
			this.manager.startTask();
		} catch (final IOException e) {
			this.logger.severe("A severe issue occurred while reading data from configuration files!");
			e.printStackTrace();
		}
	}

	public void load() {
	}

	public void log(@NotNull final String line) {
		this.log(format(text(line)));
	}

	public void log(@NotNull final Component line) {
		this.audiences.console().sendMessage(line);
	}

	public @NotNull JavaPlugin getBootstrap() {
		return this.plugin;
	}

	private void registerCommands() {
		this.handler = new CommandHandler(this);
	}

	public @NotNull PersistentPictureManager getPictureManager() {
		return this.manager;
	}

	public @NotNull AudioConfiguration getAudioConfiguration() {
		return this.audioConfiguration;
	}

	public @NotNull HttpServer getHttpServer() {
		return this.server;
	}

	public @Nullable MediaBot getMediaBot() {
		return this.mediaBot;
	}

	public @NotNull MediaLibraryCore library() {
		return this.library;
	}

	public @NotNull BukkitAudiences audience() {
		return this.audiences;
	}
}
